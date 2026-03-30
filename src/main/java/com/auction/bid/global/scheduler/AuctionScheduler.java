package com.auction.bid.global.scheduler;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionRepository;
import com.auction.bid.domain.bid.BidDto;
import com.auction.bid.domain.member.MemberService;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.auction.bid.global.scheduler.ConstAuction.AUCTION;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    @Qualifier("productRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final SchedulerService schedulerService;
    private final MemberService memberService;
    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void openAuction(Long productId, LocalDateTime auctionStart, LocalDateTime auctionEnd) {
        Auction scheduleAuction = auctionRepository.findFirstByProductIdAndMemberIsNullOrderByIdAsc(productId)
                .orElseThrow();

        log.info("auction open={}", auctionStart);
        schedulerService.changeAuctionPhase(productId, ProductBidPhase.ONGOING);

        HashOperations<String, Long, List<BidDto>> openedAuctionRedis = redisTemplate.opsForHash();
        openedAuctionRedis.put(AUCTION, scheduleAuction.getId(), new ArrayList<>());
        long ttl = Duration.between(auctionStart, auctionEnd).getSeconds() + 3600;
        redisTemplate.expire(AUCTION, ttl, TimeUnit.SECONDS);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void closeAuction(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow();
        Auction scheduleAuction = auctionRepository.findFirstByProductIdAndMemberIsNullOrderByIdAsc(productId)
                .orElseThrow();

        log.info("auction close={}", productId);
        schedulerService.changeAuctionPhase(productId, ProductBidPhase.ENDED);

        HashOperations<String, Long, List<BidDto>> openedAuctionRedis = redisTemplate.opsForHash();
        List<BidDto> bidDtoList = BidDto.convertToBidDtoList(openedAuctionRedis.get(AUCTION, scheduleAuction.getId()));

        BidDto successBidDto = bidDtoList.isEmpty() ? new BidDto() : bidDtoList.get(bidDtoList.size() - 1);
        Long finalAmount = successBidDto.getBidAmount();
        Long finalBuyerId = successBidDto.getMemberId();

        Map<Long, Long> withDrawMap = new HashMap<>();
        for (int i = bidDtoList.size() - 2; i >= 0; i--) {
            BidDto bidDto = bidDtoList.get(i);
            if (Objects.equals(bidDto.getMemberId(), finalBuyerId)) {
                continue;
            }
            Long memberId = bidDto.getMemberId();
            Long bidAmount = bidDto.getBidAmount();

            if (withDrawMap.containsKey(memberId)) {
                continue;
            }
            withDrawMap.put(memberId, bidAmount);
        }

        processWithdrawalsAndPayout(product, withDrawMap, finalBuyerId, finalAmount);

        schedulerService.saveBids(scheduleAuction.getId(), bidDtoList);
        log.info("bid save done");

        schedulerService.saveAuction(finalBuyerId, scheduleAuction.getId(), finalAmount, bidDtoList);
        log.info("auction save done");

        schedulerService.saveSale(finalBuyerId, productId, finalAmount);
        log.info("sale save done");

        openedAuctionRedis.delete(AUCTION, scheduleAuction.getId());
    }

    private void processWithdrawalsAndPayout(Product product, Map<Long, Long> withDrawMap, Long successMemberId, Long finalAmount) {
        if (successMemberId != null) {
            memberService.addMoney(product.getMember().getId(), finalAmount);
        }

        for (Map.Entry<Long, Long> entry : withDrawMap.entrySet()) {
            memberService.withDraw(entry.getKey(), entry.getValue());
        }
    }
}
