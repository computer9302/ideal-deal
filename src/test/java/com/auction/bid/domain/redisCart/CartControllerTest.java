package com.auction.bid.domain.redisCart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @InjectMocks
    private CartController cartController;

    @Mock
    private  CartService cartService;

    @Test
    void addToCart_ShouldReturnSuccessMessage(){
        //Given
        String userId = "123";
        CartItem item = new CartItem("1", "콜드브루 블랙", 3, 2500);
        //When
        ResponseEntity<String> response = cartController.addToCart(userId, item);
        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Item added to cart", response.getBody());
    }

}