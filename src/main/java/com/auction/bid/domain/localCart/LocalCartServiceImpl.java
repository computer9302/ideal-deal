package com.auction.bid.domain.localCart;

import com.auction.bid.domain.redisCart.CartItem;
import com.auction.bid.domain.redisCart.CartService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailParseException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Qualifier("localCartService")
public class LocalCartServiceImpl implements CartService {
    private final LocalCartRepository localCartRepository;

    public LocalCartServiceImpl(LocalCartRepository localCartRepository) {
        this.localCartRepository = localCartRepository;
    }

    @Override
    public void addToCart(String userId, CartItem cartItem) {
        localCartRepository.addOrUpdate(userId, cartItem.getProductId(), cartItem.getQuantity());
    }

    @Override
    public List<CartItem> getCart(String userId){
        // {prodId -> qty }를 List<CartItem> 으로 변환
        return localCartRepository.findAll(userId).entrySet().stream()
                .map(e -> {
                    CartItem i = new CartItem();
                    i.setProductId(e.getKey());
                    i.setQuantity(e.getValue());
                    return i;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void removeFromCart(String userId, CartItem item, int quantityToRemove) {

        validateRemoveArgs(userId, item, quantityToRemove);

        Map<String, Integer> map = localCartRepository.findAll(userId);
        Integer existing = map.get(item.getProductId());
        if (existing == null){
            throw new IllegalStateException("No such item in cart"); // 혹은 커스텀 예외
        }
        int newQty = existing-quantityToRemove;
        localCartRepository.updateQuantity(userId, item.getProductId(), newQty);
    }

    private void validateRemoveArgs(String userId, CartItem item, int qty){
        if (userId == null || userId.isBlank()){
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (item == null){
            throw new IllegalArgumentException("cartItem must not be null");
        }
        if (item.getProductId() == null || item.getProductId().isBlank()){
            throw new IllegalArgumentException("productId must not be blank");
        }
        if (qty <= 0){
            throw new IllegalArgumentException("quantityToRemove must be positive");
        }
    }

    @Override
    public void clearCart(String userId) {
        localCartRepository.clear(userId);
    }
}
