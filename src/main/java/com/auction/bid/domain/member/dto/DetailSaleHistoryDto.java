package com.auction.bid.domain.member.dto;

import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.sale.Sale;
import com.auction.bid.domain.sale.SaleStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class DetailSaleHistoryDto {

    private String title;
    private long startBid;
    private LocalDateTime auctionStart;
    private LocalDateTime auctionEnd;

    private SaleStatus saleStatus;
    private Long saleAmount;

    private Long buyerMemberId;
    private List<BidHistoryDto> bidHistoryDtoList;

    public static DetailSaleHistoryDto fromSale(
            Sale sale,
            LocalDateTime auctionStart,
            LocalDateTime auctionEnd,
            List<BidHistoryDto> bidHistoryDtoList) {

        Product product = sale.getProduct();

        return DetailSaleHistoryDto.builder()
                .title(product.getTitle())
                .startBid(product.getStartBid())
                .auctionStart(auctionStart)
                .auctionEnd(auctionEnd)
                .saleStatus(sale.getSaleStatus())
                .saleAmount(sale.getSalePrice())
                .buyerMemberId(sale.getBuyerMemberId())
                .bidHistoryDtoList(bidHistoryDtoList)
                .build();
    }
}
