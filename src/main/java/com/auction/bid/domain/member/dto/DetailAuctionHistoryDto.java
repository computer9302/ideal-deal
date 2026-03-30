package com.auction.bid.domain.member.dto;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionStatus;
import com.auction.bid.domain.product.Product;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class DetailAuctionHistoryDto {

    private String sellerNickname;
    private String title;
    private long startBid;
    private LocalDateTime auctionStart;
    private LocalDateTime auctionEnd;

    private AuctionStatus auctionStatus;
    private Long finalAmount;

    private Long currentMemberId;
    private List<BidHistoryDto> bidHistoryDtoList;

    public static DetailAuctionHistoryDto fromAuction(
            Auction auction,
            Long memberId,
            List<BidHistoryDto> bidHistoryDtoList) {

        Product product = auction.getProduct();
        return DetailAuctionHistoryDto.builder()
                .sellerNickname(product.getMember().getNickname())
                .title(product.getTitle())
                .startBid(product.getStartBid())
                .auctionStart(auction.getAuctionStart())
                .auctionEnd(auction.getAuctionEnd())

                .auctionStatus(auction.getAuctionStatus())
                .finalAmount(auction.getAuctionWinnerPrice())
                .currentMemberId(memberId)
                .bidHistoryDtoList(bidHistoryDtoList)
                .build();
    }
}
