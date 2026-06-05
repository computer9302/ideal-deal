package com.auction.bid.domain.member.controller;

import com.auction.bid.domain.member.dto.EmailDto;
import com.auction.bid.domain.member.dto.SignUpDto;
import com.auction.bid.domain.memberAddress.MemberAddress;
import com.auction.bid.global.security.ConstSecurity;
import com.auction.bid.global.security.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Properties;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class MemberControllerIntegrTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTUtil jwtUtil;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean(name = "productRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private ValueOperations<String, Object> valueOperations;

    SignUpDto.Request signUpReq;
    SignUpDto.Response signUpRes;

    @BeforeEach()
    public void setUp() {
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
    void member_signup_success() throws Exception {
        postPerform("/auth/signup", signUpReq)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nickname").value("testNickname"))
                .andExpect(jsonPath("$.loginId").value("testLoginId"))
                .andExpect(jsonPath("$.name").value("testName"));
    }

    @Test
    @DisplayName("이메일 보내기 성공")
    void server_send_email_success() throws Exception {
        EmailDto emailDto = EmailDto.builder()
                .email("kongminoo@naver.com")
                .build();

        postPerform("/auth/send-email", emailDto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("kongminoo@naver.com"));
    }

    @Test
    @DisplayName("ROLE 멤버 로그아웃 성공")
    void only_role_member_logged_out() throws Exception {
        UUID uuid = UUID.randomUUID();
        String accessToken = jwtUtil.generateAccessToken(
                uuid,
                ConstSecurity.ROLE_MEMBER,
                5000L
        );

        mockMvc.perform(post("/api/member/auth/logout")
                        .header(ConstSecurity.AUTHORIZATION, ConstSecurity.BEARER + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(uuid.toString()));
    }

    @Test
    @DisplayName("ROLE 어드민 로그아웃 실패")
    void admin_role_denied() throws Exception {
        UUID uuid = UUID.randomUUID();
        String accessToken = jwtUtil.generateAccessToken(
                uuid,
                ConstSecurity.ROLE_ADMIN,
                5000L
        );

        mockMvc.perform(post("/api/member/auth/logout")
                        .header(ConstSecurity.AUTHORIZATION, ConstSecurity.BEARER + accessToken))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private ResultActions postPerform(String uri, Object requestDto) throws Exception {
        return mockMvc.perform(post("/api/member" + uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());
    }
}
