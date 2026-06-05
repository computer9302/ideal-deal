package com.auction.bid.domain.auction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    Optional<Auction> findFirstByProductIdAndMemberIsNullOrderByIdAsc(Long productId);
}
