package com.auction.bid.domain.redisCart;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Qualifier("redisCartService")
public class CartServiceImpl implements CartService{

    private final CartRepository cartRepository;

    public CartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // 장바구니에 상품 추가
    public void addToCart(String userId, CartItem cartItem){
        cartRepository.addToCart(userId, cartItem);
    }

    // 장바구니 목록 조회
    @Override
    public List<CartItem> getCart(String userId) {

        return cartRepository.getCart(userId);
    }

    // 장바구니 항목 제거
    public void removeFromCart(String userId, CartItem item, int quantityToRemove){
        cartRepository.removeFromCart(userId, item, quantityToRemove);
    }

    // 장바구니 비우기
    public void clearCart(String userId){
        cartRepository.clearCart(userId);
    }
}
