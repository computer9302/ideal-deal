package com.auction.bid.domain.localCart;

import com.auction.bid.domain.redisCart.CartItem;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.convert.DataSizeUnit;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalCartServiceImplTest {

    LocalCartRepository mockRepository;
    LocalCartServiceImpl localCartServiceImpl;
    final String userId = "user123";

    @BeforeEach
    void setUp(){
        // Given: 매 테스트마다 깨끗한 mock 레포지토리와 서비스 인스턴스 준비
        mockRepository = mock(LocalCartRepository.class);
        localCartServiceImpl = new LocalCartServiceImpl(mockRepository);
    }

    @Test
    void getCart_emptyCart_returnsEmptyList() {
        // Given: 레포지토리가 빈 맵을 리턴하도록 설정
        when(mockRepository.findAll(userId))
                .thenReturn(Collections.emptyMap());

        // When: 서비스의 getCart를 호출하면
        List<CartItem> result = localCartServiceImpl.getCart(userId);

        // Then: null 이 아니고, 빈 리스트여야 한다
        assertNotNull(result, "getCart() should never return null");
        assertTrue(result.isEmpty(), "empty repository -> empty cart list");

        // And: 레포지토리의 findAll(userId) 호출이 1번 일어났음을 검증
        verify(mockRepository, times(1)).findAll(userId);
    }

    @Test
    void getCart_withItems_returnsCorrectCartItems(){
        // Given: 레포지토리가 두 개의(productId+quantity) 맵을 리턴하도록 설정
        Map<String, Integer> fakeMap = Map.of(
                "prodA", 2,
                "prodB", 5
        );
        when(mockRepository.findAll(userId))
                .thenReturn(fakeMap);

        // When: 서비스의 getCart를 호출하면
        List<CartItem> result = localCartServiceImpl.getCart(userId);

        // Then: 리스트 크기와 각 CartItem의 값이 정확해야 한다.
        assertEquals(2, result.size(), "should return two items");

        // Map(productId+quantity) 로 바궈서 검증
        Map<String, Integer> quantities = result.stream()
                .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));

        assertEquals(2, quantities.get("prodA"));
        assertEquals(5, quantities.get("prodB"));

        // And: 역시 findAll 호출이 1회
        verify(mockRepository, times(1)).findAll(userId);
    }

    @Test
    void getCart_whenRepositoryThrows_thenPropagatesException(){
        // Given: findAll() 호출 시 런타임 예외를 던지도록 설정
        when(mockRepository.findAll(userId))
                .thenThrow(new IllegalStateException("DB is down"));

        // When & Then: 서비스의 getCart() 호출 시 동일한 예외가 터지는지 확인
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> localCartServiceImpl.getCart(userId)
        );
        assertEquals("DB is down", ex.getMessage());

        // 그리고 findAll()이 1회 호출됐음을 검증
        verify(mockRepository, times(1)).findAll(userId);
    }

    private CartItem item(String productId, int qty){
        CartItem ci = new CartItem();
        ci.setProductId(productId);
        ci.setQuantity(qty);
        return ci;
    }

    @Test
    @DisplayName("given: 기존 수량 5, when: 2개 삭제, then: updateQuantity(user, prod, 3) 호출")
    void removeFromCart_decreaseQuantity_success(){
        // given
        String userId = "user123";
        String productId = "prodA";
        CartItem cartItem = item(productId, 2);

        Map<String, Integer> fake = new HashMap<>();
        fake.put(productId, 5);
        when(mockRepository.findAll(userId)).thenReturn(fake);

        // when
        localCartServiceImpl.removeFromCart(userId, cartItem, 2);

        // then
        verify(mockRepository, times(1)).updateQuantity(userId, productId, 3);
        verify(mockRepository, times(1)).findAll(userId);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("given: 기존 수량 2, when: 2개 삭제, then: updateQuantity(user, prod, 0) 호출(레포는 0이하면 삭제 처리)")
    void removeFromCart_removeEntry_whenBecomesZero(){
        // given
        String userId = "user123";
        String productId = "prodB";
        CartItem cartItem = item(productId, 2);

        Map<String, Integer> fake = new HashMap<>();
        fake.put(productId, 2);
        when(mockRepository.findAll(userId)).thenReturn(fake);

        // when
        localCartServiceImpl.removeFromCart(userId, cartItem, 2);

        // then
        verify(mockRepository, times(1))
                .updateQuantity(userId, productId, 0); // 레포에서 0이면 remove(productId)
        verify(mockRepository, times(1)).findAll(userId);
        verifyNoMoreInteractions(mockRepository);
    }


    @Test
    @DisplayName("given: 장바구니에 상품이 없음, when: 삭제 요청, then: IllegalArgumentException 발생")
    void removeFromCart_noSuchItem_throws(){
        // given
        String userId = "user123";
        String productId = "prodX";
        CartItem cartItem = item(productId, 1);

        Map<String, Integer> fake = new HashMap<>(); // empty
        when(mockRepository.findAll(userId)).thenReturn(fake);

        // when & then
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> localCartServiceImpl.removeFromCart(userId, cartItem, 1));
        assertTrue(ex.getMessage() == null || ex.getMessage().toLowerCase().contains("no such"),
                "예외 메시지에 'no such' 유사 문구가 있으면 친절합니다.");

        verify(mockRepository, times(1)).findAll(userId);
        verify(mockRepository, never()).updateQuantity(anyString(), anyString(), anyInt());
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("given: 음수/0 개수 요청, when: 삭제 요청, then: 잘못된 입력 처리(예외) - (선택)")
    void removeFromCart_invalidRemoveCount_throws() {
        // given
        String userId = "user123";
        String productId = "prodA";
        CartItem cartItem = item(productId, 0);

        Map<String, Integer> fake = new HashMap<>();
        fake.put(productId, 5);
        when(mockRepository.findAll(userId)).thenReturn(fake);

        // when & then
        assertThrows(IllegalArgumentException.class,
                ()->localCartServiceImpl.removeFromCart(userId, cartItem, 0));


        verify(mockRepository, never()).findAll(userId);
        verify(mockRepository, never()).updateQuantity(any(), any(), anyInt());
    }

    @Test
    @DisplayName("given: 장바구니에 아이템이 있음, when: clear(user), then: 해당 사용자 장바구니 비워짐")
    void clear_removesAllItems_forUser(){
        // given
        LocalCartRepository repo = new LocalCartRepository();
        String userId = "user123";
        repo.updateQuantity(userId, "prodA", 2);
        repo.updateQuantity(userId, "prodB", 3);
        assertFalse(repo.findAll(userId).isEmpty(), "사전조건: 장바구니가 비어있지 않아야 함");

        // when
        repo.clear(userId);

        // then
        Map<String, Integer> after = repo.findAll(userId);
        assertNotNull(after);
        assertTrue(after.isEmpty(), "clear 이후에는 빈 맵이어야 함");
    }

    @Test
    @DisplayName("given: 이미 빈 장바구니, when: clear(user), then: 예외 없이 여전히 빈 맵")
    void clear_onEmptyCart_noException(){
        // given
        LocalCartRepository repo = new LocalCartRepository();
        String userId = "emptyUser";
        assertTrue(repo.findAll(userId).isEmpty());
    }

    @Test
    @DisplayName("given: 두 사용자 데이터, when: A만 clear, then: B의 장바구니는 보존")
    void clear_isUserScoped_onlyTargetUserRemoved(){
        // given
        LocalCartRepository repo = new LocalCartRepository();
        String userA = "userA";
        String userB = "userB";
        repo.updateQuantity(userA, "prodA", 1);
        repo.updateQuantity(userB, "prodB", 5);

        // when
        repo.clear(userA);

        // then
        assertTrue(repo.findAll(userA).isEmpty(), "A는 비워져야 함");
        Map<String, Integer> bCart = repo.findAll(userB);
        assertEquals(1, bCart.size());
        assertEquals(5, bCart.get("prodB"));
    }

    @Test
    @DisplayName("clear는 멱등(idempotent)해야 한다: 여러 번 호출해도 결과 동일")
    void clear_isIdempotent(){
        //given
        LocalCartRepository repo = new LocalCartRepository();
        String userId = "user123";
        repo.updateQuantity(userId, "prodA", 2);

        // when
        repo.clear(userId);
        repo.clear(userId);

        // then
        assertTrue(repo.findAll(userId).isEmpty());
    }
}