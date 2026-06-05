package com.auction.bid.global.exception.exceptions;

import com.auction.bid.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.dao.DataAccessException;

@Getter
public class CartOperationException extends RuntimeException{

    private final ErrorCode errorCode;

    public CartOperationException(ErrorCode errorCode, DataAccessException ex) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
