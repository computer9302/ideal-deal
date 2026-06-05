package com.auction.bid.domain.redisCart;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public interface CartService {

   void addToCart(String userId, CartItem cartItem);

   List<CartItem> getCart(String userId);

   void removeFromCart(String userId, CartItem item, int quantityToRemove);

   void clearCart(String userId);
}
