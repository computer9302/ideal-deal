package com.auction.bid.domain.localCart;

import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class LocalCartRepository {

    private final ConcurrentHashMap<String, Map<String, Integer>> storage = new ConcurrentHashMap<>();

    public void addOrUpdate(String userId, String productId, int delta){
        storage
                .computeIfAbsent(userId, k->new HashMap<>())
                .merge(productId, delta, Integer::sum);
    }

    public void updateQuantity(String userId, String productId, int newQty){

        Map<String, Integer> cart = storage.computeIfAbsent(userId, k->new HashMap<>());

            if (newQty > 0) cart.put(productId, newQty);
            else            cart.remove(productId);

    }

    public Map<String, Integer> findAll(String userId){
        return new HashMap<>(storage.getOrDefault(userId, Collections.emptyMap()));
    }

    public void clear(String userId){
        storage.remove(userId);
    }
}
