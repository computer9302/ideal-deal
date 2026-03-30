package com.auction.bid.domain.MemberSocial;

import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.memberSocial.MemberSocial;
import com.auction.bid.domain.memberSocial.MemberSocialRepository;
import com.auction.bid.domain.memberSocial.SocialProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
public class MemberSocialRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberSocialRepository memberSocialRepository;

    @Test
    void save_and_findProviderAndProviderUserId() {
        // given
        Member m = memberRepository.save(
                Member.builder()
                        .email("a@a.com").password("pw").nickname("nick")
                        .phoneNumber("010-1111-2222")
                        .build()
        );

        MemberSocial s = memberSocialRepository.save(
                MemberSocial.of(m, SocialProvider.GOOGLE, "google-sub-123")
        );

        // when
        var found = memberSocialRepository
                .findByProviderAndProviderUserId(SocialProvider.GOOGLE, "google-sub-123")
                .orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getMember().getId()).isEqualTo(m.getId());
        assertThat(found.getProvider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(found.getProviderUserId()).isEqualTo("google-sub-123");
    }

    @Test
    void unique_member_provider_must_hold(){
        // given
        Member m  = memberRepository.save(
                Member.builder()
                        .email("b@a.com").password("pw").nickname("b").phoneNumber("010-0000-0000")
                        .build()
        );
        memberSocialRepository.save(MemberSocial.of(m, SocialProvider.KAKAO, "kakao-1"));

        // when & then: 같은 회원 +같은 provider 중복 금지
        assertThatThrownBy(()->
            memberSocialRepository.saveAndFlush(MemberSocial.of(m, SocialProvider.KAKAO, "kakao-2"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void unique_provider_user_id_must_hold(){
        // given
        Member m1 = memberRepository.save(
                Member.builder()
                        .email("x@a.com")
                        .password("pw")
                        .nickname("x")
                        .phoneNumber("010-1")
                        .build()
        );
        Member m2 = memberRepository.save(
                Member.builder()
                        .email("y@a.com")
                        .password("pw")
                        .nickname("y")
                        .phoneNumber("010-2")
                        .build()
        );
        memberSocialRepository.save(MemberSocial.of(m1, SocialProvider.NAVER, "naver-777"));

        // when & then: 같은 공급자ID를 다른 회원에 다시 연결 금지
        assertThatThrownBy(()->
                memberSocialRepository.saveAndFlush(MemberSocial.of(m2, SocialProvider.NAVER, "naver-777"))
                ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
