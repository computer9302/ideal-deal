package com.auction.bid.domain.member.dto;

import com.auction.bid.domain.bid.Bid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BidHistoryDto {

    private Long productId;
    private Long memberId;
    private Long bidAmount;
    private LocalDateTime bidTime;

    public static BidHistoryDto fromBidEntity(Bid bid) {
        return BidHistoryDto.builder()
                .productId(bid.getAuction().getProduct().getId())
                .memberId(bid.getMember().getId())
                .bidAmount(bid.getBidAmount())
                .bidTime(bid.getBidTime())
                .build();
    }

}
