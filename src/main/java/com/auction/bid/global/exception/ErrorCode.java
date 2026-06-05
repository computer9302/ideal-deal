package com.auction.bid.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ProductException
    INVALID_AUCTION_START_TIME_NOW_AFTER("경매 시작 시간은 현재 시간 이후여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_AUCTION_END_TIME_START_AFTER("경매 종료 시간은 시작 시간 이후여야 합니다..", HttpStatus.BAD_REQUEST),
    DUPLICATE_PRODUCT("이미 존재하는 상품입니다", HttpStatus.BAD_REQUEST),
    NOT_EXISTS_PRODUCT("상품이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),

    // MemberException
    NOT_EXIST_LOGIN_ID("해당 아이디가 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    NOT_EXIST_MEMBER("해당 회원이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    ALREADY_EXIST_LOGIN_ID("이미 존재하는 아이디입니다.", HttpStatus.BAD_REQUEST),

    // MailException
    CAN_NOT_SEND_MAIL("메일을 전송하지 못했습니다.", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED("메일을 인증하지 못했습니다.", HttpStatus.BAD_REQUEST),

    // AuthException
    AUTHENTICATION_FAILED("인증에 실패했습니다.", HttpStatus.BAD_REQUEST),
    TOKEN_NOT_FOUND("해당 토큰은 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND("역할 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("접근할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_ACCESS("잘못된 접근입니다.", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.BAD_REQUEST),

    // PhotoException
    PHOTO_UPLOAD_FAILED("사진 업로드중 오류가 발생했습니다.", HttpStatus.BAD_REQUEST),
    PHOTO_NOT_FOUND("사진을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),

    // CategoryException
    NOT_FOUND_CATEGORY("해당 카테고리는 존재하지 않습니다.", HttpStatus.BAD_REQUEST),

    // BidException
    NOT_EXIST_AUCTION("해당 경매는 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    BID_AMOUNT_TOO_LOW("현재 경매가보다 입찰 금액이 높아야합니다.", HttpStatus.BAD_REQUEST),

    // MoneyException
    NOT_ENOUGH_MONEY("돈이 충분하지 않습니다.", HttpStatus.BAD_REQUEST),

    // SocketException
    FAILED_TO_CONNECT_WS("웹소켓 연결에 실패하였습니다.", HttpStatus.BAD_REQUEST),

    // CartException
    FAILED_TO_ADD_ITEM_TO_CART("장바구니에 상품을 추가하는데 실패하였습니다.", HttpStatus.BAD_REQUEST),
    FAILED_TO_RETRIEVE_CART("장바구니 조회에 실패하였습니다.", HttpStatus.BAD_REQUEST),
    FAILED_TO_DELETE_ITEM_CART("장바구니 상품 삭제에 실패하였습니다.", HttpStatus.BAD_REQUEST),
    FAILED_TO_CLEAR_ITEM_CART("장바구니 비우기에 실패하였습니다.", HttpStatus.BAD_REQUEST);



    private final String description;
    private final HttpStatus httpStatus;
}
