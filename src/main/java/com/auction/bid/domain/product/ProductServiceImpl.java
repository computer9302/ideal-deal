package com.auction.bid.domain.product;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionRepository;
import com.auction.bid.domain.bid.BidDto;
import com.auction.bid.domain.category.Category;
import com.auction.bid.domain.category.CategoryRepository;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.photo.Photo;
import com.auction.bid.domain.photo.PhotoRepository;
import com.auction.bid.domain.product.dto.*;
import com.auction.bid.domain.sale.Sale;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.CategoryException;
import com.auction.bid.global.exception.exceptions.MemberException;
import com.auction.bid.global.exception.exceptions.PhotoException;
import com.auction.bid.global.exception.exceptions.ProductException;
import com.auction.bid.global.querydsl.QueryDslRepository;
import com.auction.bid.global.scheduler.AuctionScheduler;
import com.auction.bid.global.security.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.auction.bid.global.scheduler.ConstAuction.AUCTION;

@Slf4j
@Service
//@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final PhotoRepository photoRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final AuctionRepository auctionRepository;
    private final TaskScheduler taskScheduler;
    private final AuctionScheduler auctionScheduler;
    private final QueryDslRepository queryDslRepository;
    @Qualifier("productRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final JWTUtil jwtUtil;

    /**
     * 상품 등록 메서드
     * @param images 업로드할 이미지 리스트
     * @param request 상품 등록 요청 데이터
     * @param token 인증 토큰
     * @return 등록된 상품 정보
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public ProductDto.Response register(List<MultipartFile> images, ProductDto.Request request, String token) {
        if (request.getAuctionStart().isBefore(LocalDateTime.now())) {
            throw new ProductException(ErrorCode.INVALID_AUCTION_START_TIME_NOW_AFTER);
        }

        if (request.getAuctionEnd().isBefore(request.getAuctionStart())) {
            throw new ProductException(ErrorCode.INVALID_AUCTION_END_TIME_START_AFTER);
        }

        UUID memberId = jwtUtil.getMemberUUIDFromToken(token);

        Member findMember = memberRepository.findByMemberUUID(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));

        Category findCategory = categoryRepository.findByCategoryName(request.getCategory())
                .orElseThrow(() -> new CategoryException(ErrorCode.NOT_FOUND_CATEGORY));

        Product product = ProductDto.Request.toEntity(request, findMember, findCategory);

        uploadPhoto(product, images);
        Product savedProduct = productRepository.save(product);
        saveAuctionSchedule(savedProduct, request.getAuctionStart(), request.getAuctionEnd());

        scheduleAuction(savedProduct.getId(), request.getAuctionStart(), request.getAuctionEnd());
        return ProductDto.Response.fromEntity(savedProduct, request.getAuctionStart(), request.getAuctionEnd());
    }

    /**
     * 사진 업로드 메서드
     * @param product 상품 엔티티
     * @param images 업로드할 이미지 리스트
     */

    private void uploadPhoto(Product product, List<MultipartFile> images){

        try {
            String uploadsDir = "src/main/resources/static/uploads/photos/";

            for (MultipartFile image : images){
                String dbFilePath = saveImage(image, uploadsDir);

                Photo photo = new Photo(dbFilePath, product);
                product.addPhoto(photo);
            }
        } catch (IOException e) {
            throw new PhotoException(ErrorCode.PHOTO_UPLOAD_FAILED);
        }

    }

    /**
     * 이미지 파일 저장
     * @param image 업로드할 이미지 파일
     * @param uploadsDir 저장 디렉토리 경로
     * @return 데이터베이스에 저장될 파일 경로
     * @throws IOException 파일 저장 중 예외
     */
    private String saveImage(MultipartFile image, String uploadsDir) throws IOException{
        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + image.getOriginalFilename();
        String filePath = uploadsDir + fileName;
        String dbFilePath = "/uploads/photos/" + fileName;

        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.write(path, image.getBytes());

        return dbFilePath;
    }

    /**
     * 경매 시작/종료 시간 스케줄링
     * @param product 스케줄링할 상품
     */
    private void scheduleAuction(Long productId, LocalDateTime auctionStart, LocalDateTime auctionEnd) {
        Instant startDate = auctionStart.atZone(Clock.systemDefaultZone().getZone()).toInstant();
        taskScheduler.schedule(() -> auctionScheduler.openAuction(productId, auctionStart, auctionEnd), startDate);

        Instant endDate = auctionEnd.atZone(Clock.systemDefaultZone().getZone()).toInstant();
        taskScheduler.schedule(() -> auctionScheduler.closeAuction(productId), endDate);
    }

    private void saveAuctionSchedule(Product product, LocalDateTime auctionStart, LocalDateTime auctionEnd) {
        Auction scheduleAuction = auctionRepository.findFirstByProductIdAndMemberIsNullOrderByIdAsc(product.getId())
                .orElse(null);

        if (scheduleAuction == null) {
            auctionRepository.save(Auction.fromSchedule(product, auctionStart, auctionEnd));
            return;
        }

        auctionRepository.save(
                Auction.builder()
                        .id(scheduleAuction.getId())
                        .member(scheduleAuction.getMember())
                        .product(product)
                        .auctionWinnerPrice(scheduleAuction.getAuctionWinnerPrice())
                        .auctionStatus(scheduleAuction.getAuctionStatus())
                        .auctionStart(auctionStart)
                        .auctionEnd(auctionEnd)
                        .build()
        );
    }

    /**
     * 상품 ID로 상품을 조회하고 상품의 상태가 ONGOING인지 확인합니다.
     *
     * @param productId 조회할 상품의 ID
     * @return 상품이 ONGOING 상태인지 여부
     * @throws ProductException 상품이 존재하지 않을 경우 예외 발생
     */
    @Override
    public boolean isOnGoing(Long productId) {
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        return findProduct.getProductBidPhase().equals(ProductBidPhase.ONGOING);
    }

    /**
     * 이번 주의 판매 순위를 가져옵니다.
     * 각 판매 순위는 주간 통계로 계산됩니다.
     *
     * @return 주간 판매 순위 (카테고리별로 정렬된 맵 형태)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, List<RankingResponse>> getRankings() {
        LocalDate now = LocalDate.now();
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = now.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);
        Pageable pageable = PageRequest.of(0, 10);

        Map<String, List<Sale>> rankingForThisWeek =
                queryDslRepository.findTop10SaleThisWeek(
                        startOfWeek,
                        endOfWeek,
                        pageable
                );

        return RankingResponse.fromSaleListToMap(rankingForThisWeek);
    }

    /**
     * 경매 시작 전(BEFORE) 상태의 상품을 페이지로 반환합니다.
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 경매 시작 전 상품 목록
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PhaseCriteriaResponse> getBidBefore(int page, int size) {
        return getPhaseResponse(page, size, ProductBidPhase.BEFORE);
    }

    /**
     * 진행 중(ONGOING) 상태의 상품을 페이지로 반환합니다.
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 진행 중인 상품 목록
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PhaseCriteriaResponse> getBidOngoing(int page, int size) {
        return getPhaseResponse(page, size, ProductBidPhase.ONGOING);
    }

    /**
     * 종료된(ENDED) 상태의 상품을 페이지로 반환합니다.
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 종료된 상품 목록
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PhaseCriteriaResponse> getBidEnded(int page, int size) {
        return getPhaseResponse(page, size, ProductBidPhase.ENDED);
    }

    /**
     * 상품 정보를 업데이트합니다.
     *
     * @param productId 수정할 상품의 ID
     * @param images 업로드할 이미지 리스트
     * @param request 상품 수정 요청 데이터
     * @param token 인증 토큰
     * @return 수정된 상품 정보
     * @throws ProductException 상품 제목 중복, 경매 시작/종료 시간 유효성 실패 시 예외 발생
     * @throws MemberException 회원 정보가 유효하지 않을 경우 예외 발생
     * @throws CategoryException 카테고리가 존재하지 않을 경우 예외 발생
     */
    @Override
    @Transactional
    public ProductDto.Response update(Long productId, List<MultipartFile> images, ProductDto.Request request, String token) {

        if (productRepository.existsByTitle(request.getTitle())){
            throw new ProductException(ErrorCode.DUPLICATE_PRODUCT);
        }

        if(request.getAuctionStart().isBefore(LocalDateTime.now())){
            throw new ProductException(ErrorCode.INVALID_AUCTION_START_TIME_NOW_AFTER);
        }
        if(request.getAuctionEnd().isBefore(request.getAuctionStart())){
            throw new ProductException(ErrorCode.INVALID_AUCTION_END_TIME_START_AFTER);
        }

        UUID memberId = jwtUtil.getMemberUUIDFromToken(token);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        Product productUpdate = Product.builder()
                .id(product.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .startBid(request.getStartBid())
                .member((memberRepository.findByMemberUUID(memberId).orElseThrow(
                        () -> new MemberException(ErrorCode.NOT_EXIST_MEMBER))))
                .category(categoryRepository.findByCategoryName(request.getCategory()).orElseThrow(
                        () -> new CategoryException(ErrorCode.NOT_FOUND_CATEGORY)))
                .build();

        Product savedProduct = productRepository.save(productUpdate);
        uploadPhoto(productUpdate, images);
        saveAuctionSchedule(savedProduct, request.getAuctionStart(), request.getAuctionEnd());
        return ProductDto.Response.fromEntity(savedProduct, request.getAuctionStart(), request.getAuctionEnd());
    }

    /**
     * 인기 상품 목록을 페이지 단위로 반환합니다.
     *
     * @param page 요청한 페이지 번호
     * @param size 한 페이지에 표시할 상품 수
     * @return 요청된 페이지의 인기 상품 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<HotResponse> getHotPage(int page, int size) {
        int start = (page - 1) * size;
        int end = page * size;
        return getHotResponse(start, end);
    }

    /**
     * 판매가 기준 가장 높은 순으로 상품 순위를 반환합니다.
     *
     * @param page 요청한 페이지 번호
     * @param size 한 페이지에 표시할 순위 수
     * @return 가장 높은 순위의 상품 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RankingResponse> getRankingsHighest(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Sale> highestPage = queryDslRepository.getHighestSaleList(pageable);
        return RankingResponse.fromSalePageToResPage(highestPage);
    }

    /**
     * 판매가 기준 가장 낮은 순으로 상품 순위를 반환합니다.
     *
     * @param page 요청한 페이지 번호
     * @param size 한 페이지에 표시할 순위 수
     * @return 가장 낮은 순위의 상품 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RankingResponse> getRankingsLowest(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Sale> lowestSaleList = queryDslRepository.getLowestSaleList(pageable);
        return RankingResponse.fromSalePageToResPage(lowestSaleList);
    }

    /**
     * Redis에 저장된 경매 데이터로부터 인기 상품을 조회합니다.
     *
     * @param start 조회 시작 인덱스
     * @param end 조회 종료 인덱스
     * @return 인기 상품 리스트
     */
    @Override
    @Transactional(readOnly = true)
    @Qualifier("productRedisTemplate")
    public List<HotResponse> getHotResponse(int start, int end) {
        HashOperations<String, Long, List<BidDto>> redisHash = redisTemplate.opsForHash();
        Map<Long, List<BidDto>> hashEntries = redisHash.entries(AUCTION);
        List<Map.Entry<Long, List<BidDto>>> sortedHashEntries = hashEntries.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .toList();

        int listSize = sortedHashEntries.size();
        if (start >= listSize) {
            return List.of();
        }

        List<Map.Entry<Long, List<BidDto>>> selectedEntries = sortedHashEntries.subList(start, Math.min(end, listSize));

        List<Long> auctionIds = selectedEntries.stream()
                .map(Map.Entry::getKey)
                .toList();

        List<Auction> auctions = auctionRepository.findAllById(auctionIds);

        Map<Long, Product> productMap = auctions.stream()
                .collect(Collectors.toMap(Auction::getId, Auction::getProduct));

        List<Product> sortedProductList = auctionIds.stream()
                .map(productMap::get)
                .filter(Objects::nonNull)
                .toList();

        return sortedProductList.stream()
                .map(HotResponse::fromProductEntity)
                .toList();
    }

    /**
     * 메인 페이지 데이터를 반환합니다.
     * 인기 상품, 경매 상태별 상품 목록을 포함합니다.
     *
     * @return 메인 페이지 응답 데이터
     */
    @Override
    @Transactional(readOnly = true)
    public MainResponse getMainPage() {
        List<HotResponse> hotPage = getHotPage(1, 10);
        Page<PhaseCriteriaResponse> beforePhase = getPhaseResponse(0, 10, ProductBidPhase.BEFORE);
        Page<PhaseCriteriaResponse> ongoingPhase = getPhaseResponse(0, 10, ProductBidPhase.ONGOING);
        Page<PhaseCriteriaResponse> endPhase = getPhaseResponse(0, 10, ProductBidPhase.ENDED);
        return new MainResponse(hotPage, beforePhase, ongoingPhase, endPhase);
    }

    /**
     * 주어진 경매 상태에 따른 상품 목록을 페이지로 반환합니다.
     *
     * @param page 요청한 페이지 번호
     * @param size 한 페이지에 표시할 상품 수
     * @param phase 경매 상태 (BEFORE, ONGOING, ENDED)
     * @return 상태별 상품 페이지
     */
    private Page<PhaseCriteriaResponse> getPhaseResponse(
            int page,
            int size,
            ProductBidPhase phase
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = queryDslRepository.getProductByPhase(phase, pageable);

        List<PhaseCriteriaResponse> resList = productPage.stream()
                .map(PhaseCriteriaResponse::fromProductEntity)
                .toList();

        return PageableExecutionUtils.getPage(resList, pageable, productPage::getTotalElements);
    }

    /**
     * 주어진 상품 ID에 해당하는 상품을 삭제합니다.
     *
     * @param productId 삭제할 상품의 ID
     */
    @Override
    @Transactional
    public void delete(Long productId){
        productRepository.deleteById(productId);
    }

    /**
     * 상품 ID로 상품을 조회합니다.
     *
     * @param productId 조회할 상품의 ID
     * @return 조회된 상품
     * @throws ProductException 상품이 존재하지 않을 경우 예외 발생
     */
    @Override
    @Transactional(readOnly = true)
    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));
    }

    /**
     * 상품 ID와 연관된 상품 및 사진 정보를 조회합니다.
     *
     * @param productId 조회할 상품의 ID
     * @return 상품 및 사진 정보
     * @throws PhotoException 사진이 존재하지 않을 경우 예외 발생
     * @throws ProductException 상품이 존재하지 않을 경우 예외 발생
     */
    @Override
    @Transactional(readOnly = true)
    public ProductGetDto.Response getProduct(Long productId){
        List<Photo> photos = photoRepository.findByProductId(productId);
        if (photos.isEmpty()){
            throw new PhotoException(ErrorCode.PHOTO_NOT_FOUND);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        Auction auction = auctionRepository.findFirstByProductIdAndMemberIsNullOrderByIdAsc(productId).orElse(null);
        return ProductGetDto.Response.fromEntity(product, photos, auction);
    }

}
