package com.auction.bid.domain.redisCart;

import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.CartOperationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CartRepositoryTest {

    @Test
    void addToCartFailed() {
        // given: 모의 객체 생성 및 예외 발생 시뮬레이션
        RedisTemplate<String, Object> mockRedisTemplate = mock(RedisTemplate.class);
        HashOperations<String, Object, Object> mockHashOps = mock(HashOperations.class);

        // RedisTemplate의 opsForHash() 호출 시, 모의 HashOperations 반환
        when(mockRedisTemplate.opsForHash()).thenReturn(mockHashOps);
        // 모의 HashOperations의 get() 호출 시 DataRetrievalFailureException 발생하도록 설정
        when(mockHashOps.get(anyString(), any())).thenThrow(new DataRetrievalFailureException("Simulated Redis Exception"));

        CartRepository cartRepository = new CartRepository(mockRedisTemplate);
        String userId = "user123";
        CartItem cartItem = CartItem.builder()
                .productId("prod123")
                .quantity(1)
                .build();

        // when & then: addToCart를 호출하면 CartOperationException이 발생해야함.
        assertThrows(CartOperationException.class, () -> {
            cartRepository.addToCart(userId, cartItem);
        });
    }

    @Test
    void addToCart_successfully_adds_new_item(){
        // GIVEN: RedisTemplate과 HashOperations를 모의(mock)로 설정
        RedisTemplate<String, Object> mockRedisTemplate = mock(RedisTemplate.class);
        HashOperations<String, Object, Object> mockHashOps = mock(HashOperations.class);

        when(mockRedisTemplate.opsForHash()).thenReturn(mockHashOps);
        // Redis에서 해당 상품이 없다고 가정(get() == null)
        when(mockHashOps.get("cart:user123", "prod123")).thenReturn(null);

        CartRepository cartRepository = new CartRepository(mockRedisTemplate);

        CartItem cartItem = CartItem.builder()
                .productId("prod123")
                .quantity(1)
                .name("Test Product")
                .price(100.0)
                .build();

        // WHEN & THEN: 예외 없이 실행되어야 하며, put()이 호출되어야 함.
        assertDoesNotThrow(() -> cartRepository.addToCart("user123", cartItem));
        verify(mockHashOps, times(1)).put("cart:user123", "prod123", cartItem);
    }

    @Test
    void addToCart_increasesQuantityIfProductExists(){
        // GIVEN
        RedisTemplate<String, Object> mockRedisTemplate = mock(RedisTemplate.class);
        HashOperations<String, Object, Object> mockHashOps = mock(HashOperations.class);
        when(mockRedisTemplate.opsForHash()).thenReturn(mockHashOps);

        String userId = "user123";
        String key = "cart:" + userId;
        String productId = "prod123";

        // 기존에 장바구니에 동일한 상품이 수량 2로 존재
        CartItem existingItem = CartItem.builder()
                .productId(productId)
                .quantity(2)
                .name("Test Product")
                .price(100.0)
                .build();

        // 새로 당ㅁ으려는 항목 (수량 3)
        CartItem newItem = CartItem.builder()
                .productId(productId)
                .quantity(3)
                .name("Test Product")
                .price(100.0)
                .build();

        // Redis에서 기존 항목 반환
        when(mockHashOps.get(key, productId)).thenReturn(existingItem);

        CartRepository cartRepository = new CartRepository(mockRedisTemplate);

        // WhEN & THEN
        assertDoesNotThrow(() -> cartRepository.addToCart(userId, newItem));

        // 수량이 2+3=5가 되어 put()이 호출되어야 함.
        verify(mockHashOps).put(eq(key), eq(productId),
                argThat(arg -> {
                    CartItem updated = (CartItem) arg;
                    return updated.getQuantity() == 5;
                }));
    }

    @Test
    void getCart(){
       // GIVEN: 모의 객체 생성 및 초기 설정
        RedisTemplate<String, Object> mockRedisTemplate = mock(RedisTemplate.class);
        HashOperations<String, Object, Object> mockHashOps = mock(HashOperations.class);

        // RedisTemplate의 opsForHash() 호출 시, 모의 HashOperations 반환
        when(mockRedisTemplate.opsForHash()).thenReturn(mockHashOps);
        // addToCart() 호출 시 , 동일 상품이 없다고 가정하여 null 반환
        when(mockHashOps.get(anyString(), any())).thenReturn(null);
        // put() 메소드는 void이므로 별도 설정 없이 진행

        // 특정 키("cart:user123") 조회 시 , 예외 발생을 시뮬레이션
        String key = "cart:user123";
        when(mockHashOps.entries(key))
                .thenThrow(new DataRetrievalFailureException("Simulated Exception"));

        CartRepository cartRepository = new CartRepository(mockRedisTemplate);
        String userId = "user123";
        CartItem cartItem = CartItem.builder()
                .productId("prod123")
                .name("desktop")
                .quantity(1)
                .price(1000000)
                .build();

        // WHEN: addToCart() 호출해서 장바구니에 데이터를 추가 (여기서는 모의 객체에 의해 put()이 호출됨)
        cartRepository.addToCart(userId, cartItem);

        // THEN: getCart() 호출 시, 위에서 시뮬레이션한 예외가 발생하여 CartOperationException 이 던져져야 함
        assertThrows(CartOperationException.class, () -> {
            cartRepository.getCart(userId);
        });
    }


    @Test
    void getCart_success_returnsEntries(){
        // ---Given: RedisTemplate과 HashOperation를 모의(mock)로 설정
        RedisTemplate<String, Object> mockRedisTemplate = mock(RedisTemplate.class);
        HashOperations<String, Object, Object> mockHashOps = mock(HashOperations.class);

        // opsForHash() 호출 시 우리가 만든 mockHashOps를 반환하도록
        when(mockRedisTemplate.opsForHash()).thenReturn(mockHashOps);

        String userId = "user123";
        String key = "cart:" + userId;

        // Redis에서 조회할 가짜 맵 데이터 준비
        Map<Object, Object> fakeEntries = new HashMap<>();
        // fakeEntries.put(, CartItem)이 되어야함. 시도해볼것
        fakeEntries.put("prodA", new CartItem("prodA", "A", 2, 100000));
        fakeEntries.put("prodB", new CartItem("prodB", "B", 3, 300000));

        // when
        // entries(key) 호출 시 fakeEntries를 반환하도록 설정
        when(mockHashOps.entries(key)).thenReturn(fakeEntries);

        List<CartItem> expected = fakeEntries.values().stream()
                .map(v -> (CartItem) v)
                .collect(Collectors.toList());

        CartRepository repository = new CartRepository(mockRedisTemplate);

        /*
        Map<Object, Object> entries = mockRedisTemplate.opsForHash().entries("cart:" + userId);
        entries.forEach((k, v) -> System.out.println(k + " -> " + v.getClass()));
         */

        // --- when: getCart() 호출 ---
        List<CartItem> result = repository.getCart(userId);


        // ---Then
        assertEquals(expected.size(), result.size());
        assertTrue(result.containsAll(expected));
    }


    @Test
    void removeFromCart_NoExistingItem() {
        // Simulated delete fauilure 에러메세지가 출력되지 않는 이유
        // 아이템이 없을 경우 예외를 터뜨리는 비즈니즈 로직을 CartRepository에 넣었기 때문에 get()==null인 경우를 테스트한다.
        // 즉 Redis내부의 delete()자체의 에러를 테스트 하는것이 아니기때문에 when(.delete(), Simulated delete fauilure)가 필요없다.
       // Given: 존재하지 않는 항목: get() 호출 시 null 반환
        String userId = "user123";
        String key = "cart:" + userId;
        CartItem cartItem = CartItem.builder()
                .productId("prod123")
                .name("Test Product")
                .quantity(1)
                .price(1000000)
                .build();
        int quantityToRemove = 1;

        RedisTemplate<String, Object> mockRedisTemplate = mock(RedisTemplate.class);
        HashOperations<String, Object, Object> mockHashOps = mock(HashOperations.class);
        when(mockRedisTemplate.opsForHash()).thenReturn(mockHashOps);
        when(mockHashOps.get("cart:" + userId, cartItem.getProductId())).thenReturn(null);

        CartRepository cartRepository = new CartRepository(mockRedisTemplate);


        // when: getCart() 호출 시, 위에서 시뮬레이션한 예외가 발생하여 CartOperationException 이 던져져야 함
        CartOperationException ex = assertThrows(CartOperationException.class, () -> {
            cartRepository.removeFromCart(userId, cartItem, quantityToRemove);
        });

        // 아이템이 null일때 예외 터지고 메세지 제대로 던져지고 있는지 확인
        assertEquals(ErrorCode.FAILED_TO_DELETE_ITEM_CART, ex.getErrorCode());

        // Then: get()이 null을 반환하였으므로, put()이 delete() 호출이 없어야 함.
        Mockito.verify(mockHashOps, Mockito.never()).put(Mockito.anyString(), Mockito.any(), Mockito.any());
        Mockito.verify(mockHashOps, Mockito.never()).delete(Mockito.anyString(), Mockito.any());

    }

    @Test
    void removeFromCart_decreaseQuantity_whenQuantityRemain(){
        // Given
        String userId = "user123";
        String key = "cart:" + userId;
        String productId = "prod123";
        // 기존 장바구니에 quantity = 5 로 들어있다고 가정
        CartItem existing = CartItem.builder()
                .productId(productId)
                .quantity(5)
                .build();
        int removeAmount = 3; // 남은 수량 = 2;

        RedisTemplate<String, Object> mockRedis = mock(RedisTemplate.class);
        HashOperations<String, Object, Object> mockHash = mock(HashOperations.class);
        when(mockRedis.opsForHash()).thenReturn(mockHash);
        when(mockHash.get(key, productId)).thenReturn(existing);

        CartRepository repo = new CartRepository(mockRedis);

        // when, then
        assertDoesNotThrow(()->repo.removeFromCart(userId, existing, removeAmount));

        // 남은 수량(2)로 업데이트 되어 put()이 호출되어야 한다.
        verify(mockHash, times(1)).put(eq(key), eq(productId), argThat(arg ->{
            CartItem updated = (CartItem) arg;
            return updated.getQuantity() == 2;
        }));
        // delete는 호출되면 안 된다.
        verify(mockHash, never()).delete(anyString(), any());
    }

    @Test
    void removeFromCart_deletesItem_whenQuantityGoesTozZeroOrLess(){
        // Given
        String userId = "user123";
        String key = "cart:" + userId;
        String productId = "prod123";
        // 기존 장바구니에 quantity = 2 로 들어있다고 가정
        CartItem existing = CartItem.builder()
                .productId(productId)
                .quantity(2)
                .build();
        int removeAmount = 3; // 남은 수량 = -1 -> 삭제

        RedisTemplate<String, Object> mockRedis = mock(RedisTemplate.class);
        HashOperations<String, Object, Object> mockHash = mock(HashOperations.class);
        when(mockRedis.opsForHash()).thenReturn(mockHash);
        when(mockHash.get(key, productId)).thenReturn(existing);
        // delete 호출 시 1L 반환
        when(mockHash.delete(key, productId)).thenReturn(1L);

        CartRepository repo = new CartRepository(mockRedis);

        //when /then
        assertDoesNotThrow(()->repo.removeFromCart(userId, existing, removeAmount));

        // 수량이 0 이하이므로 delete()호출
        verify(mockHash, times(1)).delete(eq(key), eq(productId));
        // put은 호출되면 안 된다.
        verify(mockHash, never()).put(anyString(), any(), any());
    }

    @Test
    void clearCart_whenDeleteFails_thenThrowCartOperationException(){
        // Given: userId와 mockRedisTemplate를 준비하고 delete() 호출 시 예외를 던지도록 설정
        String userId = "user123";
        String key = "cart:" + userId;

        RedisTemplate<String, Object> mockRedis = mock(RedisTemplate.class);
        when(mockRedis.delete(key))
                .thenThrow(new DataRetrievalFailureException("Simulated delete failure"));

        CartRepository repo = new CartRepository(mockRedis);

        // When & Then: clearCart() 호출 시 CartOperationExcption으로 전환되는지 검증
        CartOperationException ex = assertThrows(
                CartOperationException.class,
                ()->repo.clearCart(userId)
        );

        // Optional: errorCode가 FFAILED_TO_CLEAR_ITEM_CART인지 확인
        assert ex.getErrorCode() == ErrorCode.FAILED_TO_CLEAR_ITEM_CART;

                // verify: delete()가 정확히 한 번 호출되었는지
        verify(mockRedis, times(1)).delete(key);
    }

    @Test
    void clearCart() {
        // GIVEN: 사용자 ID 및 모의RedisTemplate 설정
        String userId = "user123";
        String key = "cart:" + userId;
        RedisTemplate<String, Object> mockRedisTemplate = mock(RedisTemplate.class);

        // delete(key) 호출 시 정상 동작하도록(dP: true 반환) 설정
        when(mockRedisTemplate.delete(key)).thenReturn(true);

        CartRepository repository = new CartRepository(mockRedisTemplate);

        // WHEN: clearCart() 호출
        // THEN: 예외 없이 정상적으로 실행되어야 하며, delete 메소드가 정확히 한 번 호출되었음을 검증
        assertDoesNotThrow(()-> repository.clearCart(userId));
        verify(mockRedisTemplate, timeout(1)).delete(key);

    }
}