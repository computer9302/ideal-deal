package com.auction.bid.domain.product.dto;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.photo.Photo;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ProductGetDto {

    @Builder
    @Getter
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private List<String> imagePath;
        private Long startBid;
        private LocalDateTime auctionStart;
        private LocalDateTime auctionEnd;
        private ProductBidPhase productBidPhase;
        private String memberName;
        private String categoryName;

        public static Response fromEntity(Product product, List<Photo> photos, Auction auction) {
            return Response.builder()
                    .id(product.getId())
                    .title(product.getTitle())
                    .description(product.getDescription())
                    .imagePath(photos.stream().map(Photo::getImagePath).collect(Collectors.toList()))
                    .startBid(product.getStartBid())
                    .auctionStart(auction != null ? auction.getAuctionStart() : null)
                    .auctionEnd(auction != null ? auction.getAuctionEnd() : null)
                    .productBidPhase(product.getProductBidPhase())
                    .memberName(product.getMember().getName())
                    .categoryName(product.getCategory().getCategoryName())
                    .build();
        }
    }
}
