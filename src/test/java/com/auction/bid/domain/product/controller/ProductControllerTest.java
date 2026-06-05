//package com.auction.bid.domain.product.controller;
//
//import com.auction.bid.domain.product.Product;
//import com.auction.bid.domain.product.ProductService;
//import com.auction.bid.domain.product.dto.ProductDto;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//class ProductControllerTest {
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private ProductService productService;
//
//    @Test
//    @DisplayName("상품 등록 성공 테스트")
//    void registerProduct() throws Exception {
//
//        MultipartFile file1 = new MockMultipartFile(
//                "files", // 컨트롤러에서 기대하는 필드명
//                "file1.jpg",
//                "image/jpeg",
//                "File1 Content".getBytes()
//        );
//
//        MultipartFile file2 = new MockMultipartFile(
//                "files",
//                "file2.jpg",
//                "image/jpeg",
//                "File2 Content".getBytes()
//        );
//
//        List<MultipartFile> files = new ArrayList<>();
//
//        files.add(file1);
//        files.add(file2);
//
//        // given : 상품을 저장하기 위한 준비 과정
//        ProductDto.Request form = ProductDto.Request.builder()
//                .title("나이키 신발")
//                .description("중고 나이키 신발입니다.")
//                .filePath(files)
//                .startBid(100000)
//                .auctionStart(LocalDateTime.parse("2024-12-08T00:00:00"))
//                .auctionEnd(LocalDateTime.parse("2024-12-30T00:00:00"))
//                .build();
//
//        // when : 실제로 상품을 저장
//        ProductDto.Response response = productService.register(form);
//
//        // then
//        Assertions.assertNotNull(response.getTitle());
//
//    }
//}