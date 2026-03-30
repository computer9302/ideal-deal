package com.auction.bid.global.querydsl;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionStatus;
import com.auction.bid.domain.bid.Bid;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.sale.Sale;
import com.auction.bid.domain.sale.SaleStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.auction.bid.domain.auction.QAuction.auction;
import static com.auction.bid.domain.bid.QBid.bid;
import static com.auction.bid.domain.member.QMember.member;
import static com.auction.bid.domain.photo.QPhoto.photo;
import static com.auction.bid.domain.product.QProduct.product;
import static com.auction.bid.domain.sale.QSale.sale;

@Repository
public class QueryDslRepositoryImpl implements QueryDslRepository{

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public QueryDslRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 이번 주의 최고 및 최저 판매 목록을 반환합니다.
     *
     * @param startOfWeek 이번 주의 시작 시간
     * @param endOfWeek 이번 주의 종료 시간
     * @param pageable 페이지 정보
     * @return 판매 목록 (최고 판매, 최저 판매)
     */
    @Override
    public Map<String, List<Sale>> findTop10SaleThisWeek(
            LocalDateTime startOfWeek,
            LocalDateTime endOfWeek,
            Pageable pageable) {

        List<Sale> saleHighestList = getSales(startOfWeek, endOfWeek, pageable, sale.salePrice.desc());
        List<Sale> saleLowestList = getSales(startOfWeek, endOfWeek, pageable, sale.salePrice.asc());

        Map<String, List<Sale>> res = new HashMap<>();
        res.put("HighestList", saleHighestList);
        res.put("LowestList", saleLowestList);
        return res;
    }

    /**
     * 최고 판매 목록을 반환합니다.
     *
     * @param pageable 페이지 정보
     * @return 최고 판매 목록
     */
    @Override
    public Page<Sale> getHighestSaleList(Pageable pageable) {
        return getSales(pageable, sale.salePrice.desc());
    }

    /**
     * 최저 판매 목록을 반환합니다.
     *
     * @param pageable 페이지 정보
     * @return 최저 판매 목록
     */
    @Override
    public Page<Sale> getLowestSaleList(Pageable pageable) {
        return getSales(pageable, sale.salePrice.asc());
    }

    /**
     * 주어진 제품 ID 목록에 해당하는 모든 제품을 반환합니다.
     *
     * @param productIds 제품 ID 목록
     * @return 제품 목록
     */
    @Override
    public List<Product> findAllByProductIds(List<Long> productIds) {
        return queryFactory
                .selectFrom(product)
                .leftJoin(product.photos).fetchJoin()
                .where(product.id.in(productIds))
                .fetch();
    }

    /**
     * 지정된 입찰 단계에 해당하는 제품 목록을 반환합니다.
     *
     * @param phase 입찰 단계
     * @param pageable 페이지 정보
     * @return 제품 목록
     */
    @Override
    public Page<Product> getProductByPhase(ProductBidPhase phase, Pageable pageable) {
        List<Product> products = queryFactory
                .selectFrom(product)
                .leftJoin(product.photos).fetchJoin()
                .where(product.productBidPhase.eq(phase))
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .leftJoin(product.photos)
                .where(product.productBidPhase.eq(phase));

        return PageableExecutionUtils.getPage(products, pageable, countQuery::fetchOne);
    }

    /**
     * 지정된 회원의 경매 목록을 반환합니다.
     *
     * @param memberUUID 회원의 UUID
     * @param pageable 페이지 정보
     * @param auctionStatus 경매 상태
     * @return 경매 목록
     */
    @Override
    public Page<Auction> getAuctionList(UUID memberUUID, Pageable pageable, AuctionStatus auctionStatus) {
        BooleanExpression condition =
                auctionStatus != null ?
                        auction.auctionStatus.eq(auctionStatus) : null;

        List<Auction> auctionList = queryFactory
                .selectFrom(auction)
                .leftJoin(auction.member, member).fetchJoin()
                .leftJoin(auction.product, product).fetchJoin()
                .leftJoin(product.photos).fetchJoin()
                .where(
                        member.memberUUID.eq(memberUUID),
                        condition
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(auction.count())
                .from(auction)
                .leftJoin(auction.member, member)
                .leftJoin(auction.product, product)
                .leftJoin(product.photos)
                .where(
                        member.memberUUID.eq(memberUUID),
                        condition
                );

        return PageableExecutionUtils.getPage(auctionList, pageable, countQuery::fetchOne);
    }

    /**
     * 주어진 제품 ID에 해당하는 모든 입찰 목록을 반환합니다.
     *
     * @param productId 제품 ID
     * @return 입찰 목록
     */
    @Override
    public List<Bid> findAllByAuctionProductId(Long productId) {
        return queryFactory
                .selectFrom(bid)
                .leftJoin(bid.auction, auction).fetchJoin()
                .leftJoin(auction.product, product).fetchJoin()
                .leftJoin(bid.member, member).fetchJoin()
                .where(product.id.eq(productId))
                .fetch();
    }

    /**
     * 경매를 로드하고 연관된 모든 엔티티를 함께 로딩합니다.
     *
     * @param auctionId 경매 ID
     * @return 경매 객체
     */
    @Override
    public Auction getAuctionEagerly(Long auctionId) {
        return queryFactory
                .selectFrom(auction)
                .leftJoin(auction.product, product).fetchJoin()
                .leftJoin(product.member).fetchJoin()
                .where(auction.id.eq(auctionId))
                .fetchOne();
    }

    /**
     * 회원의 판매 목록을 반환합니다.
     *
     * @param memberUUID 회원 UUID
     * @param pageable 페이지 정보
     * @param saleStatus 판매 상태
     * @return 판매 목록
     */
    @Override
    public List<Sale> getSaleList(UUID memberUUID, Pageable pageable, SaleStatus saleStatus) {
        BooleanExpression condition =
                saleStatus != null ?
                        sale.saleStatus.eq(saleStatus) : null;

        return queryFactory
                .selectFrom(sale)
                .leftJoin(sale.member, member).fetchJoin()
                .leftJoin(sale.product, product).fetchJoin()
                .leftJoin(product.photos).fetchJoin()
                .where(
                        member.memberUUID.eq(memberUUID),
                        condition
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * 판매를 로드하고 연관된 모든 엔티티를 함께 로딩합니다.
     *
     * @param saleId 판매 ID
     * @return 판매 객체
     */
    @Override
    public Sale getSaleEagerly(Long saleId) {
        return queryFactory
                .selectFrom(sale)
                .leftJoin(sale.product, product).fetchJoin()
                .leftJoin(sale.member).fetchJoin()
                .where(sale.id.eq(saleId))
                .fetchOne();
    }

    /**
     * 이번 주의 판매 목록을 가져옵니다. (판매 성공 상태, 주어진 기간 내)
     *
     * @param startOfWeek 시작 시간 (이번 주 시작)
     * @param endOfWeek 종료 시간 (이번 주 종료)
     * @param pageable 페이지 정보
     * @param orderSpecifier 정렬 조건
     * @return 판매 목록
     */
    private List<Sale> getSales(LocalDateTime startOfWeek, LocalDateTime endOfWeek, Pageable pageable, OrderSpecifier<?> orderSpecifier) {
        return queryFactory
                .selectFrom(sale)
                .leftJoin(sale.product, product).fetchJoin()
                .leftJoin(product.photos, photo).fetchJoin()
                .where(
                        sale.saleStatus.eq(SaleStatus.SALE_SUCCESS),
                        sale.createdAt.between(startOfWeek, endOfWeek)
                )
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * 판매 목록을 가져옵니다. (판매 성공 상태)
     *
     * @param pageable 페이지 정보
     * @param orderSpecifier 정렬 조건
     * @return 판매 목록
     */
    private Page<Sale> getSales(Pageable pageable, OrderSpecifier<?> orderSpecifier) {
        List<Sale> saleList = queryFactory
                .selectFrom(sale)
                .leftJoin(sale.product, product).fetchJoin()
                .leftJoin(product.photos, photo).fetchJoin()
                .where(
                        sale.saleStatus.eq(SaleStatus.SALE_SUCCESS)
                )
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(sale.count())
                .from(sale)
                .leftJoin(sale.product, product)
                .leftJoin(product.photos, photo)
                .where(sale.saleStatus.eq(SaleStatus.SALE_SUCCESS));

        return PageableExecutionUtils.getPage(saleList, pageable, countQuery::fetchOne);
    }

}
