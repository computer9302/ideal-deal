package com.auction.bid.domain.redisCart;

import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.CartOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.auction.bid.global.exception.ErrorCode.FAILED_TO_ADD_ITEM_TO_CART;

@Repository
public class CartRepository {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public CartRepository(@Qualifier("cartRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 사용자별 장바구니의 Redis key 생성(예:"cart:user123")
    private String getKey(String userId){
        return "cart:" + userId;
    }

    // 장바구니 항목 추가 또는 업데이트
    // 동일 productId가 있으면 기존 cartItem의 수량을 증가
    public void addToCart(String userId, CartItem cartItem){
        try {
            String key = getKey(userId);
            // 이미 같은 상품이 존재하는지 확인
            Object existingObj = redisTemplate.opsForHash().get(key, cartItem.getProductId());
            if (existingObj != null) {
                CartItem existingItem = (CartItem) existingObj;
                // 수량을 누적해서 증가
                existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
                redisTemplate.opsForHash().put(key, cartItem.getProductId(), existingItem);
            } else {
                // 없으면 새 CartItem 추가
                redisTemplate.opsForHash().put(key, cartItem.getProductId(), cartItem);
            }
        }catch (DataAccessException ex){
            logger.error("Error adding item to cart for user : " + userId, ex);
            // 예외를 던지거나 적절한 방식으로 처리 (예: 실패 메시지 반환)
            // 이 코드는 불필요한 코드이다. http에게 response로 예외를 보내는 코드이다.
            // 하지만 addToCart()는 Postman 테스트가 불필요하다.
            throw new CartOperationException(FAILED_TO_ADD_ITEM_TO_CART, ex);
        }
    }

    // 장바구니 목록 조회
    public List<CartItem> getCart(String userId){
        try {
            // 1) Redis에서 Map<Object, Object>로 꺼내고
            Map<Object, Object> entries =  redisTemplate.opsForHash().entries("cart:"+userId);

            // 2) 각 Map의 value를 CartItem으로 캐스팅한 뒤
            // 3) List<CartItem> 으로 수집해 리턴
            return entries.values().stream()
                    .map(v -> (CartItem) v)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex){
            logger.error("Error retrieving cart for user : " + userId, ex);
            throw new CartOperationException(ErrorCode.FAILED_TO_RETRIEVE_CART, ex);
        }


    }

    // 장바구니 항목 삭제
    public void removeFromCart(String userId, CartItem item, int quantityToRemove){

        String key = "cart:" + userId;

        // RedisTemplate의 key, hashKey serializer 가져오기
        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();

        /*
        // RedisInsight에서 사람이 보기 편하게 하기 위한 코드, TDD 테스트때 오류 발생 주석 처리
        // 키와 필드를 직렬화 (바이트 배열로 변환)
        byte[] serializedKeyBytes = keySerializer.serialize(key);
        byte[] serializedFieldBytes = hashKeySerializer.serialize(item.getProductId());

        // 바이트 배열을 UTF-8 문자열로 변환하여 로그 출력
        String serializedKey = new String(serializedKeyBytes, StandardCharsets.UTF_8);
        String serializedField = new String(serializedFieldBytes, StandardCharsets.UTF_8);

        System.out.println("Serialized Key: " + serializedKey);
        System.out.println("Serialized Field: " + serializedField);
*/

        try{
            CartItem currentItem = (CartItem) redisTemplate.opsForHash().get(key, item.getProductId());
            if (currentItem == null){
                // 없으면 예외 던지기
                throw new CartOperationException(
                        ErrorCode.FAILED_TO_DELETE_ITEM_CART,
                        new DataRetrievalFailureException("No cart item for productId=" + item.getProductId())
                );
            }
                int newQuantity = currentItem.getQuantity()-quantityToRemove;
                if (newQuantity > 0){
                    currentItem.setQuantity(newQuantity);
                    redisTemplate.opsForHash().put(key, item.getProductId(), currentItem);
                }else {
                    Long removedCount = redisTemplate.opsForHash().delete(key, item.getProductId());
                    System.out.println("Deleted count: " + removedCount);
                }

        }catch (DataAccessException ex){
            logger.error("Error deleting cart for user : " + userId, ex);
            throw new CartOperationException(ErrorCode.FAILED_TO_DELETE_ITEM_CART, ex);
        }
    }

    // 장바구니 비우기
    public void clearCart(String userId){

        try {
            redisTemplate.delete("cart:" + userId);
        }catch (DataAccessException ex){
            logger.error("Error clearing cart for user : " + userId, ex);
            throw new CartOperationException(ErrorCode.FAILED_TO_CLEAR_ITEM_CART, ex);
        }
    }
}
