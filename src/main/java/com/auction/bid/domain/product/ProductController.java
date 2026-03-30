package com.auction.bid.domain.product;

import com.auction.bid.domain.product.dto.ProductDto;
import com.auction.bid.global.security.ConstSecurity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ProductController
 * 상품 등록, 수정, 삭제 및 조회를 위한 REST 컨트롤러
 */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 등록
     *
     * @param images 업로드된 이미지 파일 목록
     * @param request 상품 등록 요청 DTO
     * @param token 사용자 인증 토큰
     * @return 등록된 상품 정보
     */
    @PostMapping("/register")
    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    public ResponseEntity<?> registerProduct(
            @Valid @RequestParam("images") List<MultipartFile> images,
            @ModelAttribute ProductDto.Request request,
            @RequestHeader(ConstSecurity.AUTHORIZATION) String token){

            return ResponseEntity.ok(productService.register(images, request, token));
    }

    /**
     * 상품 수정
     *
     * @param productId 수정할 상품 ID
     * @param images 업로드된 이미지 파일 목록
     * @param request 상품 수정 요청 DTO
     * @param token 사용자 인증 토큰
     * @return 수정된 상품 정보
     */
    @PutMapping("/{productId}")
    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    public ResponseEntity<?> updateProduct(@PathVariable(name = "productId") Long productId,
                                           @Valid @RequestParam("images") List<MultipartFile> images,
                                           @ModelAttribute ProductDto.Request request,
                                           @RequestHeader(ConstSecurity.AUTHORIZATION) String token){

        return ResponseEntity.ok(productService.update(productId, images, request, token));
    }

    /**
     * 상품 삭제
     *
     * @param id 삭제할 상품 ID
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    public void deleteProduct(@PathVariable(name = "productId") Long id){
        productService.delete(id);
    }

    /**
     * 특정 상품 정보 조회
     *
     * @param productId 조회할 상품 ID
     * @return 조회된 상품 정보
     */
    @GetMapping("/{productId}")

    public ResponseEntity<?> getProduct(@PathVariable(name = "productId") Long productId){
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    /**
     * 메인 상품 조회
     *
     * @return 추천 상품 목록
     */
    @GetMapping
    public ResponseEntity<?> getMainPage() {
        return ResponseEntity.ok(productService.getMainPage());
    }

    /**
     * 입찰 시작 전 상품 조회
     *
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 입찰 시작 전 상품 목록
     */
    @GetMapping("/bid/before")
    public ResponseEntity<?> getBifBefore(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getBidBefore(page, size));
    }

    /**
     * 진행 중인 입찰 상품 조회
     *
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 진행 중인 입찰 상품 목록
     */
    @GetMapping("/bid/ongoing")
    public ResponseEntity<?> getBidOngoing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getBidOngoing(page, size));
    }

    /**
     * 종료된 입찰 상품 조회
     *
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 종료된 입찰 상품 목록
     */
    @GetMapping("/bid/ended")
    public ResponseEntity<?> getBidEnded(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getBidEnded(page, size));
    }

    /**
     * 낙찰된 금액 순위 조회
     *
     * @return 판매 순위 목록
     */
    @GetMapping("/sale/rankings")
    public ResponseEntity<?> getRankings() {
        return ResponseEntity.ok(productService.getRankings());
    }

    /**
     * 최고가 순위 조회
     *
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 최고가 순위 목록
     */
    @GetMapping("/sale/rankings/highest")
    public ResponseEntity<?> getRankingsHighest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getRankingsHighest(page, size));
    }

    /**
     * 최저가 순위 조회
     *
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 최저가 순위 목록
     */
    @GetMapping("/sale/rankings/lowest")
    public ResponseEntity<?> getRankingsLowest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getRankingsLowest(page, size));
    }

    /**
     * 입찰이 많이 된 상품 조회
     *
     * @param page 페이지 번호 (기본값: 1)
     * @param size 페이지 크기 (기본값: 10)
     * @return 인기 상품 목록
     */
    @GetMapping("/hot")
    public ResponseEntity<?> getHotPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getHotPage(page, size));
    }

}
