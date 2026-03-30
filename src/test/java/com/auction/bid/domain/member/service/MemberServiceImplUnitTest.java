package com.auction.bid.domain.member.service;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionRepository;
import com.auction.bid.domain.bid.Bid;
import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.member.MemberServiceImpl;
import com.auction.bid.domain.member.dto.DetailSaleHistoryDto;
import com.auction.bid.domain.member.dto.SignUpDto;
import com.auction.bid.domain.memberAddress.MemberAddress;
import com.auction.bid.domain.memberAddress.MemberAddressRepository;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.sale.Sale;
import com.auction.bid.domain.sale.SaleStatus;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.AuthException;
import com.auction.bid.global.exception.exceptions.MailException;
import com.auction.bid.global.exception.exceptions.MemberException;
import com.auction.bid.global.querydsl.QueryDslRepository;
import com.auction.bid.global.security.ConstSecurity;
import com.auction.bid.global.security.RefreshTokenRepository;
import com.auction.bid.global.security.jwt.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplUnitTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    ValueOperations<String, Object> valueOperations;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    JWTUtil jwtUtil;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    MemberAddressRepository memberAddressRepository;

    @Mock
    QueryDslRepository queryDslRepository;

    @Mock
    AuctionRepository auctionRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private MemberServiceImpl memberService;

    SignUpDto.Request signUpReq;
    SignUpDto.Response signUpRes;

    @Captor
    ArgumentCaptor<MemberAddress> addrCaptor;


    @BeforeEach
    void setUp() {
        signUpReq = SignUpDto.Request.builder()
                .loginId("testLoginId")
                .password("1234567890")
                .email("test@naver.com")
                .nickname("testNickname")
                .name("testName")
                .phoneNumber("010-1234-5678")
                .emailVerified(true)
                .addressRequest(
                        MemberAddress.builder()
                                .city("seoul")
                                .street("saemalo")
                                .zipcode("548")
                                .build())
                .build();

        signUpRes = SignUpDto.Response.builder()
                .id(1L)
                .loginId("resLoginId")
                .name("resName")
                .nickname("resNickName")
                .build();
    }

    @Test
    @DisplayName("회원 가입 성공")
    void signUp_success() throws Exception {
        when(memberRepository.existsByLoginId(anyString()))
                .thenReturn(false);
        when(memberRepository.save(any()))
                .thenReturn(SignUpDto.Request.toEntity(signUpReq, "pass"));
        when(memberAddressRepository.save(any(MemberAddress.class)))
                .thenReturn(SignUpDto.Request.toAddressEntity(signUpReq, SignUpDto.Request.toEntity(signUpReq, "pass") , true));

        SignUpDto.Response actual = memberService.signUp(signUpReq);

        assertEquals(signUpReq.getLoginId(), actual.getLoginId());
        assertEquals(signUpReq.getName(), actual.getName());
        assertEquals(signUpReq.getNickname(), actual.getNickname());

        verify(memberAddressRepository).save(addrCaptor.capture());
        MemberAddress saved = addrCaptor.getValue();

        assertEquals(signUpReq.getAddressRequest().getCity(), saved.getCity());
        assertEquals(signUpReq.getAddressRequest().getStreet(), saved.getStreet());
        assertEquals(signUpReq.getAddressRequest().getZipcode(), saved.getZipcode());
        assertEquals(actual.getId(), saved.getMember().getId());
    }

    @Test
    @DisplayName("이메일 인증 예외")
    void emailVerified_false_ret_ex() throws Exception {
        SignUpDto.Request req =
                SignUpDto.Request.builder()
                        .emailVerified(false)
                        .build();

        MailException exception = assertThrows(MailException.class,
                () -> memberService.signUp(req));

        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하는 아이디는 예외")
    void loginId_exists_ret_ex() throws Exception {
        when(memberRepository.existsByLoginId(anyString()))
                .thenReturn(true);

        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.signUp(signUpReq));

        assertEquals(ErrorCode.ALREADY_EXIST_LOGIN_ID, exception.getErrorCode());
    }

    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail_success() throws Exception {
        String email = "test";
        String token = "token";

        when(redisTemplate.hasKey(email)).thenReturn(true);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(email)).thenReturn(token);

        boolean actual = memberService.verifyEmail(email, token);

        assertTrue(actual);
    }

    @Test
    @DisplayName("레디스에서 이메일의 키가 없으면 예외")
    void no_key_ret_ex() throws Exception {
        String email = "test";
        String token = "token";


        when(redisTemplate.hasKey(email))
                .thenReturn(false);

        MailException exception = assertThrows(MailException.class,
                () -> memberService.verifyEmail(email, token));

        assertEquals(ErrorCode.TOKEN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        String token = ConstSecurity.BEARER +  "token";
        String jwtToken = "token";
        String memberId = UUID.randomUUID().toString();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtUtil.getTokenFromHeader(anyString())).thenReturn(jwtToken);
        when(jwtUtil.getMemberStrUUIDFromToken(anyString())).thenReturn(memberId);

        String actual = memberService.logout(token);

        assertEquals(memberId, actual);

        verify(valueOperations).set(jwtToken, ConstSecurity.BLACK_LIST, 1, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("유효하지 않는 토큰은 예외")
    void invalid_token_ret_ex() throws Exception {
        String token = null;

        AuthException exception = assertThrows(AuthException.class,
                () -> memberService.logout(token));

        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("판매 상세 조회는 schedule auction의 시간 정보를 사용한다")
    void getSaleDetail_uses_schedule_auction_time() {
        Long productId = 10L;
        LocalDateTime auctionStart = LocalDateTime.of(2026, 3, 25, 10, 0);
        LocalDateTime auctionEnd = LocalDateTime.of(2026, 3, 25, 11, 0);

        Product product = Product.builder()
                .id(productId)
                .title("camera")
                .startBid(1000L)
                .build();

        Sale sale = Sale.builder()
                .id(1L)
                .product(product)
                .salePrice(1500L)
                .saleStatus(SaleStatus.SALE_SUCCESS)
                .buyerMemberId(3L)
                .build();

        Auction scheduleAuction = Auction.builder()
                .id(20L)
                .product(product)
                .auctionStart(auctionStart)
                .auctionEnd(auctionEnd)
                .build();

        Bid bid = Bid.builder()
                .id(100L)
                .auction(scheduleAuction)
                .member(com.auction.bid.domain.member.Member.builder().id(2L).build())
                .bidAmount(1500L)
                .bidTime(auctionEnd.minusMinutes(5))
                .build();

        when(queryDslRepository.getSaleEagerly(1L)).thenReturn(sale);
        when(queryDslRepository.findAllByAuctionProductId(productId)).thenReturn(List.of(bid));
        when(auctionRepository.findFirstByProductIdAndMemberIsNullOrderByIdAsc(productId))
                .thenReturn(Optional.of(scheduleAuction));

        DetailSaleHistoryDto actual = memberService.getSaleDetail(1L);

        assertEquals(auctionStart, actual.getAuctionStart());
        assertEquals(auctionEnd, actual.getAuctionEnd());
        assertEquals(1, actual.getBidHistoryDtoList().size());
        assertEquals(productId, actual.getBidHistoryDtoList().get(0).getProductId());
    }

}
