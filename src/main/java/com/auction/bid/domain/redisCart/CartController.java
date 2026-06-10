package com.auction.bid.domain.redisCart;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(@Qualifier("localCartService") CartService cartService) {
        this.cartService = cartService;
    }

    // 장바구니에 항목 추가
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestParam String userId, @RequestBody @Valid CartItem cartItem){
        cartService.addToCart(userId, cartItem);
        return ResponseEntity.ok("Item added to cart");
    }

    // 장바구니 목록 조회
    @GetMapping("/{userId}")
    public ResponseEntity<List<CartItem>> getCart(@PathVariable String userId){
        List<CartItem> cartItems = cartService.getCart(userId);
        return ResponseEntity.ok(cartItems);
    }

    // 장바구니 항목 제거
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> removeFromCart(@PathVariable String userId, @RequestBody CartItem item){
        cartService.removeFromCart(userId, item, item.getQuantity());
        return ResponseEntity.ok("Item removed from cart");
    }

    // 장바구니 비우기
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<String> clearCart(@PathVariable String userId){
        cartService.clearCart(userId);
        return ResponseEntity.ok("Cart cleared");
    }
}
