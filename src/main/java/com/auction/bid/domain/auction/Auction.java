package com.auction.bid.domain.auction;

import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.product.Product;
import com.auction.bid.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long id;

    @Column(name = "auction_winner_price")
    private Long auctionWinnerPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_status")
    private AuctionStatus auctionStatus;

    @Column(name = "auction_start")
    private LocalDateTime auctionStart;

    @Column(name = "auction_end")
    private LocalDateTime auctionEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public static Auction fromSchedule(Product product, LocalDateTime auctionStart, LocalDateTime auctionEnd) {
        return Auction.builder()
                .product(product)
                .auctionStart(auctionStart)
                .auctionEnd(auctionEnd)
                .build();
    }

    public static Auction fromBid(
            Member member,
            Product product,
            Long finalAmount,
            AuctionStatus auctionStatus,
            LocalDateTime auctionStart,
            LocalDateTime auctionEnd
    ) {
        return Auction.builder()
                .member(member)
                .product(product)
                .auctionWinnerPrice(finalAmount)
                .auctionStatus(auctionStatus)
                .auctionStart(auctionStart)
                .auctionEnd(auctionEnd)
                .build();
    }
}
