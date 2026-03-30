package com.auction.bid.domain.member.controller;

import com.auction.bid.domain.member.MemberController;
import com.auction.bid.domain.member.MemberService;
import com.auction.bid.domain.member.dto.EmailDto;
import com.auction.bid.domain.member.dto.SignUpDto;
import com.auction.bid.domain.member.dto.TokenVerificationDto;
import com.auction.bid.domain.memberAddress.MemberAddress;
import com.auction.bid.global.security.ConstSecurity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerUnitTest {

    @MockitoBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private SignUpDto.Request signUpReq;
    private SignUpDto.Response signUpRes;

    @BeforeEach()
    public void setUp() {
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
    @WithMockUser
    @DisplayName("회원가입 성공")
    void signup_success() throws Exception {
        when(memberService.signUp(any())).thenReturn(signUpRes);

        postPerform("/auth/signup", signUpReq)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.loginId").value("resLoginId"))
                .andExpect(jsonPath("$.nickname").value("resNickName"));

        verify(memberService, times(1)).signUp(any());
    }

    @Test
    @DisplayName("Dto 누락했을 시 예외반환")
    @WithMockUser
    void emptyDto_throws_ex() throws Exception {
        signUpReq = SignUpDto.Request.builder()
                .addressRequest(
                        MemberAddress.builder()
                                .city("seoul")
                                .street("saemalo")
                                .zipcode("548")
                                .build())
                .build();

        postPerform("/auth/signup", signUpReq)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", containsInAnyOrder(
                        "아이디를 입력해주세요.",
                        "휴대폰 번호를 입력해주세요.",
                        "이메일을 입력해주세요.",
                        "닉네임을 입력해주세요.",
                        "이름을 입력해주세요.",
                        "비밀번호를 입력해주세요.",
                        "must not be null"
                )));
    }

    @Test
    @DisplayName("이메일 전송 성공")
    @WithMockUser
    void server_send_email() throws Exception {
        EmailDto emailDto = EmailDto.builder()
                .email("kongminoo@naver.com")
                .build();

        when(memberService.sendEmail(anyString()))
                .thenReturn("success@naver.com");

        postPerform("/auth/send-email", emailDto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("success@naver.com"));
    }

    @Test
    @DisplayName("이메일 인증 성공")
    @WithMockUser
    void correct_token_returns_true() throws Exception {
        TokenVerificationDto tvDto = TokenVerificationDto.builder()
                .email("test@naver.com")
                .token("a1s2d3")
                .build();

        when(memberService.verifyEmail(anyString(), anyString()))
                .thenReturn(true);

        postPerform("/auth/verify-email", tvDto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("로그아웃 성공")
    @WithMockUser
    void logout_success() throws Exception {
        String token = "token";

        when(memberService.logout(anyString()))
                .thenReturn("memberId");

        mockMvc.perform(post("/api/member/auth/logout")
                .header(ConstSecurity.AUTHORIZATION, ConstSecurity.BEARER + token)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("memberId"));
    }

    private ResultActions postPerform(String uri, Object requestDto) throws Exception {
        return mockMvc.perform(post("/api/member" + uri)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());
    }

}