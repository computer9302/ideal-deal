package com.auction.bid.domain.redisCart;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {
    @NotNull
    private String productId;
    @NotNull
    private String name;
    @NotNull
    private int quantity;
    @NotNull
    private double price;

}
