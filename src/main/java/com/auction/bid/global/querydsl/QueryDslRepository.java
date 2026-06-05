package com.auction.bid.global.querydsl;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionStatus;
import com.auction.bid.domain.bid.Bid;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.sale.Sale;
import com.auction.bid.domain.sale.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface QueryDslRepository {

    Map<String, List<Sale>> findTop10SaleThisWeek(LocalDateTime startOfWeek, LocalDateTime endOfWeek, Pageable pageable);

    Page<Sale> getHighestSaleList(Pageable pageable);

    Page<Sale> getLowestSaleList(Pageable pageable);

    List<Product> findAllByProductIds(List<Long> productIds);

    Page<Product> getProductByPhase(ProductBidPhase phase, Pageable pageable);

    Page<Auction> getAuctionList(UUID memberUUID, Pageable pageable, AuctionStatus auctionStatus);

    List<Bid> findAllByAuctionProductId(Long productId);

    Auction getAuctionEagerly(Long auctionId);

    List<Sale> getSaleList(UUID memberUUID, Pageable pageable, SaleStatus saleStatus);

    Sale getSaleEagerly(Long saleId);

}
