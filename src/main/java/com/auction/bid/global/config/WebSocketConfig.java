package com.auction.bid.global.config;

import com.auction.bid.domain.auction.AuctionRepository;
import com.auction.bid.domain.member.MemberService;
import com.auction.bid.domain.product.ProductService;
import com.auction.bid.global.security.jwt.JWTUtil;
import com.auction.bid.global.websocket.CustomHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;
    private final JWTUtil jwtUtil;
    private final MemberService memberService;
    private final ProductService productService;
    private final AuctionRepository auctionRepository;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/bid/*")
                .setAllowedOrigins("*")
                .addInterceptors(new CustomHandshakeInterceptor(jwtUtil, memberService, productService, auctionRepository));
    }

}
