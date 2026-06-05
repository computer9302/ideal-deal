package com.auction.bid.global.scheduler;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionRepository;
import com.auction.bid.domain.auction.AuctionStatus;
import com.auction.bid.domain.bid.BidDto;
import com.auction.bid.domain.bid.BidRepository;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.product.ProductRepository;
import com.auction.bid.domain.sale.Sale;
import com.auction.bid.domain.sale.SaleRepository;
import com.auction.bid.domain.sale.SaleStatus;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.ProductException;
import com.auction.bid.global.websocket.WebSocketBidHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SchedulerService {

    private final ProductRepository productRepository;
    private final BidRepository bidRepository;
    private final MemberRepository memberRepository;
    private final AuctionRepository auctionRepository;
    private final SaleRepository saleRepository;
    private final WebSocketBidHandler webSocketBidHandler;

    public void changeAuctionPhase(Long productId, ProductBidPhase productBidPhase) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));
        Auction scheduleAuction = auctionRepository.findFirstByProductIdAndMemberIsNullOrderByIdAsc(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        product.changeAuctionPhase(productBidPhase);
        productRepository.save(product);
        webSocketBidHandler.phaseChange(scheduleAuction.getId(), productBidPhase);
    }

    public void saveBids(Long auctionId, List<BidDto> bidDtoList) {
        if (bidDtoList.isEmpty()) {
            return;
        }

        Auction findAuction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        List<Long> memberIds = bidDtoList.stream()
                .map(BidDto::getMemberId)
                .distinct()
                .toList();

        List<Member> findMemberList = memberRepository.findAllById(memberIds);
        Map<Long, Member> memberMap = findMemberList.stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        bidDtoList.stream()
                .map(bidDto -> BidDto.toBidEntity(
                        bidDto,
                        memberMap.get(bidDto.getMemberId()),
                        findAuction)
                )
                .forEach(bidRepository::save);
    }

    public void saveAuction(Long winnerId, Long auctionId, Long finalAmount, List<BidDto> bidDtoList) {
        if (bidDtoList.isEmpty()) {
            return;
        }

        Auction scheduleAuction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));
        Product findProduct = scheduleAuction.getProduct();

        List<Long> memberIds = bidDtoList.stream()
                .map(BidDto::getMemberId)
                .distinct()
                .toList();

        List<Member> findMemberList = memberRepository.findAllById(memberIds);

        findMemberList.forEach(member -> {
            if (Objects.equals(member.getId(), winnerId)) {
                auctionRepository.save(Auction.fromBid(
                        member,
                        findProduct,
                        finalAmount,
                        AuctionStatus.BID_SUCCESS,
                        scheduleAuction.getAuctionStart(),
                        scheduleAuction.getAuctionEnd()
                ));
            } else {
                auctionRepository.save(Auction.fromBid(
                        member,
                        findProduct,
                        finalAmount,
                        AuctionStatus.BID_FAILURE,
                        scheduleAuction.getAuctionStart(),
                        scheduleAuction.getAuctionEnd()
                ));
            }
        });
    }

    public void saveSale(Long buyerId, Long productId, Long finalAmount) {
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        if (buyerId == null) {
            saleRepository.save(
                    Sale.fromAuction(null, null, SaleStatus.SALE_FAILURE, findProduct.getMember(), findProduct)
            );
            return;
        }

        saleRepository.save(
                Sale.fromAuction(buyerId, finalAmount, SaleStatus.SALE_SUCCESS, findProduct.getMember(), findProduct)
        );
    }
}
