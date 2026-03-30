package com.auction.bid.global.websocket;

import com.auction.bid.domain.bid.BidDto;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberService;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.product.ProductRepository;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.MoneyException;
import com.auction.bid.global.exception.exceptions.ProductException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static com.auction.bid.global.scheduler.ConstAuction.AUCTION;
import static com.auction.bid.global.websocket.ConstWebsocket.AUCTION_ID;
import static com.auction.bid.global.websocket.ConstWebsocket.MEMBER;
import static com.auction.bid.global.websocket.ConstWebsocket.PRODUCT_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketBidHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    @Qualifier("productRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberService memberService;

    private final Set<WebSocketSession> sessions = new HashSet<>();
    private final Map<Long, Set<WebSocketSession>> roomSessionMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        Member findMember = (Member) attributes.get(MEMBER);
        Long auctionId = (Long) attributes.get(AUCTION_ID);

        sendViewerCountToBidRoom(getSocketSetWhenEntered(auctionId, session));
        sessions.add(session);

        List<BidDto> bidDtoList = BidDto.convertToBidDtoList(bidListFromRedis(auctionId));
        BidDto bidDto = bidDtoList.isEmpty() ? BidDto.emptyDtoList(auctionId)
                : bidDtoList.get(bidDtoList.size() - 1);

        sendMessageToEntrant(bidDto, session);

        if (findMember == null) {
            log.info("[sessionId={}] connected", session.getId());
        } else {
            log.info("[memberId={}] connected", findMember.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Member member = (Member) attributes.get(MEMBER);

        if (member == null) {
            sendMessage(session, "Login is required before bidding.");
            return;
        }

        Long productId = (Long) attributes.get(PRODUCT_ID);
        Long auctionId = (Long) attributes.get(AUCTION_ID);
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        if (findProduct.getProductBidPhase() == ProductBidPhase.ENDED) {
            sendMessage(session, "Auction has already ended.");
            return;
        }

        if (findProduct.getProductBidPhase() == ProductBidPhase.BEFORE) {
            sendMessage(session, "Auction has not started yet.");
            return;
        }

        Set<WebSocketSession> bidRoomSessions = getWebSocketSessions(auctionId);
        if (Objects.equals(findProduct.getMember().getId(), member.getId())) {
            sendMessage(session, "Seller cannot place a bid.");
            return;
        }

        MessageDto.Request dtoRequest = objectMapper.readValue(message.getPayload(), MessageDto.Request.class);
        List<BidDto> bidList = bidListFromRedis(auctionId);

        Long currMaxBidAmount = getMaxBidAmount(bidList, findProduct.getStartBid());
        if (currMaxBidAmount >= dtoRequest.getBidAmount()) {
            sendMessage(session, "Bid amount must be higher than the current max bid.");
            return;
        }

        if (dtoRequest.getMaxBidLimit() < dtoRequest.getBidAmount()) {
            sendMessage(session, "Bid amount cannot exceed the max bid limit.");
            return;
        }

        if (!bidList.isEmpty()) {
            List<BidDto> bidDtos = BidDto.convertToBidDtoList(bidList);
            BidDto bidDto = bidDtos.get(bidDtos.size() - 1);
            if (Objects.equals(bidDto.getMemberId(), member.getId())) {
                sendMessage(session, "You are already the highest bidder.");
                return;
            }
        }

        try {
            Long lastMoney = (Long) attributes.get("lastMoney");
            if (lastMoney == null) {
                lastMoney = 0L;
            }

            memberService.bidToAuction(member, dtoRequest.getBidAmount(), lastMoney);
            attributes.put("lastMoney", dtoRequest.getBidAmount());
        } catch (MoneyException | NullPointerException e) {
            log.info("MoneyEx={}", e.getMessage());
            sendMessage(session, "Not enough balance.");
            return;
        }

        MessageDto.Response response = MessageDto.Response
                .fromRequest(member, productId, dtoRequest.getBidAmount());

        putInRedis(auctionId, member, dtoRequest, bidList);
        sendMessageToBidRoom(response, bidRoomSessions);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Map<String, Object> attributes = session.getAttributes();
        Member member = (Member) attributes.get(MEMBER);
        Long auctionId = (Long) attributes.get(AUCTION_ID);

        if (member == null) {
            log.info("{} disconnected", session.getId());
            return;
        }

        Set<WebSocketSession> roomSessions = roomSessionMap.get(auctionId);
        if (roomSessions != null) {
            roomSessions.remove(session);
        }
        sessions.remove(session);
        sendViewerCountToBidRoom(getWebSocketSessions(auctionId));
        log.info("{} disconnected", member.getId());
    }

    public void phaseChange(Long auctionId, ProductBidPhase phase) {
        Set<WebSocketSession> webSocketSessions = getWebSocketSessions(auctionId);
        webSocketSessions.parallelStream()
                .filter(WebSocketSession::isOpen)
                .forEach(session -> sendMessage(session, phase));
        if (phase == ProductBidPhase.ENDED) {
            closeAllSessions(webSocketSessions);
        }
    }

    private void sendMessageToBidRoom(MessageDto.Response res, Set<WebSocketSession> bidRoomSession) {
        bidRoomSession.parallelStream()
                .filter(WebSocketSession::isOpen)
                .forEach(sess -> sendMessage(sess, res));
    }

    private void sendViewerCountToBidRoom(Set<WebSocketSession> bidRoomSession) {
        String viewerCnt = "viewerCount : " + bidRoomSession.size();
        bidRoomSession.parallelStream()
                .filter(WebSocketSession::isOpen)
                .forEach(sess -> sendMessage(sess, viewerCnt));
    }

    private <T> void sendMessage(WebSocketSession session, T message) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void sendMessageToEntrant(BidDto bidDto, WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(bidDto)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private Set<WebSocketSession> getWebSocketSessions(Long auctionId) {
        if (!roomSessionMap.containsKey(auctionId)) {
            roomSessionMap.put(auctionId, new HashSet<>());
        }

        return roomSessionMap.get(auctionId);
    }

    private Set<WebSocketSession> getSocketSetWhenEntered(Long auctionId, WebSocketSession webSocketSession) {
        if (!roomSessionMap.containsKey(auctionId)) {
            roomSessionMap.put(auctionId, new HashSet<>());
        }

        Set<WebSocketSession> webSocketSessions = roomSessionMap.get(auctionId);
        if (webSocketSessions.size() >= 5) {
            removeClosedSession(webSocketSessions);
        }

        webSocketSessions.add(webSocketSession);
        return webSocketSessions;
    }

    private void removeClosedSession(Set<WebSocketSession> bidRoomSession) {
        bidRoomSession.removeIf(sess -> !sessions.contains(sess));
    }

    private List<BidDto> bidListFromRedis(Long auctionId) {
        HashOperations<String, Long, List<BidDto>> auctionRedis = redisTemplate.opsForHash();
        if (!auctionRedis.hasKey(AUCTION, auctionId)) {
            return new ArrayList<>();
        }

        return auctionRedis.get(AUCTION, auctionId);
    }

    private void closeAllSessions(Set<WebSocketSession> bidRoomSession) {
        Iterator<WebSocketSession> iterator = bidRoomSession.iterator();

        while (iterator.hasNext()) {
            WebSocketSession session = iterator.next();
            if (session.isOpen()) {
                iterator.remove();
                sessions.remove(session);
                try {
                    session.close();
                } catch (IOException e) {
                    log.error("Failed to close websocket session: sessionId={}", session.getId(), e);
                }
            }
        }
    }

    private void closeOneSession(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        Long auctionId = (Long) attributes.get(AUCTION_ID);
        Set<WebSocketSession> roomSessions = roomSessionMap.get(auctionId);
        if (roomSessions != null) {
            roomSessions.remove(session);
        }

        try {
            sendMessage(session, "Auction is not available.");
            sessions.remove(session);
            session.close();
        } catch (IOException e) {
            log.error("Failed to close websocket session: sessionId={}", session.getId(), e);
        }
    }

    private void putInRedis(Long auctionId, Member member, MessageDto.Request dtoRequest, List<BidDto> bidList) {
        bidList.add(BidDto.builder()
                .auctionId(auctionId)
                .memberId(member.getId())
                .nickname(member.getNickname())
                .bidAmount(dtoRequest.getBidAmount())
                .bidTime(LocalDateTime.now())
                .build());
        HashOperations<String, Long, List<BidDto>> auctionRedis = redisTemplate.opsForHash();
        auctionRedis.put(AUCTION, auctionId, bidList);
    }

    private Long getMaxBidAmount(List<BidDto> bidList, long startBid) {
        if (bidList.isEmpty()) {
            return startBid - 1;
        }

        return BidDto.convertToBidDtoList(bidList)
                .get(bidList.size() - 1)
                .getBidAmount();
    }
}
