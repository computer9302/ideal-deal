package com.auction.bid.global.oauth2;

import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.memberSocial.MemberSocial;
import com.auction.bid.domain.memberSocial.MemberSocialRepository;
import com.auction.bid.domain.memberSocial.SocialProvider;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.AuthException;
import com.auction.bid.global.oauth2.userinfo.GoogleUserInfo;
import com.auction.bid.global.oauth2.userinfo.KakaoUserInfo;
import com.auction.bid.global.oauth2.userinfo.NaverUserInfo;
import com.auction.bid.global.oauth2.userinfo.OAuth2UserInfo;
import com.auction.bid.global.security.ConstSecurity;
import com.auction.bid.global.security.RefreshToken;
import com.auction.bid.global.security.RefreshTokenRepository;
import com.auction.bid.global.security.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberSocialRepository memberSocialRepository;

    @Value("${spring.jwt.redirect}")
    private String REDIRECT_URI;

    @Value("${spring.jwt.access-token.expiration-time}")
    private long ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${spring.jwt.refresh-token.expiration-time}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;

        // "google" / "kakao" / "naver"
        String registrationId = token.getAuthorizedClientRegistrationId();
        SocialProvider provider = SocialProvider.from(registrationId);

        OAuth2UserInfo oAuth2UserInfo;
        switch (registrationId) {
            case "google" -> oAuth2UserInfo = new GoogleUserInfo(token.getPrincipal().getAttributes());
            case "kakao" -> oAuth2UserInfo = new KakaoUserInfo(token.getPrincipal().getAttributes());
            case "naver" -> oAuth2UserInfo = new NaverUserInfo((Map<String, Object>) token.getPrincipal().getAttributes().get("response"));
            default -> oAuth2UserInfo = null;
        }

        if (oAuth2UserInfo == null) {
            throw new AuthException(ErrorCode.ACCESS_DENIED);
        }

        String providerUserId = oAuth2UserInfo.getProviderId();
        String name = oAuth2UserInfo.getName();
        Optional<MemberSocial> findMember = memberSocialRepository.findByProviderAndProviderUserId(provider, providerUserId);
        Member member;
        if (findMember.isEmpty()) {
            UUID uuid = UUID.randomUUID();
            member = memberRepository.save(
                    Member.builder()
                            .memberUUID(uuid)
                            .name(name)
                            .role(ConstSecurity.ROLE_MEMBER)
                            .nickname(uuid.toString().substring(0, 6))
                            .build());

            memberSocialRepository.save(MemberSocial.of(member, provider, providerUserId));
        } else {
            member = findMember.get().getMember();
            Optional<RefreshToken> existingToken = refreshTokenRepository.findByMemberId(member.getMemberUUID());
            existingToken.ifPresent(refreshTokenRepository::delete);
            // refresh토큰을 아래에서 재발행하기위해 바로 Flush날림
            refreshTokenRepository.flush();
        }

        String refreshToken = jwtUtil
                .generateRefreshToken(
                        member.getMemberUUID(),
                        ConstSecurity.ROLE_MEMBER,
                        REFRESH_TOKEN_EXPIRATION_TIME
                );

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .memberId(member.getMemberUUID())
                        .token(refreshToken)
                        .build()
        );

        String accessToken = jwtUtil
                .generateAccessToken(
                        member.getMemberUUID(),
                        ConstSecurity.ROLE_MEMBER,
                        ACCESS_TOKEN_EXPIRATION_TIME
                );

        String encodedName = URLEncoder.encode(name, "UTF-8");
        String redirectUri = String.format(REDIRECT_URI, encodedName, accessToken, refreshToken);
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }

}
