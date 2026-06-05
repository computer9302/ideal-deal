package com.auction.bid.domain.member.service;

import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.member.MemberService;
import com.auction.bid.domain.member.dto.SignUpDto;
import com.auction.bid.domain.memberAddress.MemberAddress;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
public class MemberServiceImplIntegrTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @MockitoBean
    JavaMailSender javaMailSender;

    @MockitoBean(name = "productRedisTemplate")
    RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    ValueOperations<String, Object> valueOperations;

    SignUpDto.Request signUpReq;
    SignUpDto.Response signUpRes;

    @BeforeEach
    void setUp() {
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

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
    void signup_success() {
        SignUpDto.Response response = memberService.signUp(signUpReq);

        assertEquals(1L, response.getId());
        assertEquals("testNickname", response.getNickname());
        assertEquals("testLoginId", response.getLoginId());
        assertEquals("testName", response.getName());
    }

    @Test
    @DisplayName("이메일 전송 성공")
    void sendEmail_success() {
        String to = "kongminoo@naver.com";
        String actual = memberService.sendEmail(to);
        assertEquals(to, actual);
    }
}
