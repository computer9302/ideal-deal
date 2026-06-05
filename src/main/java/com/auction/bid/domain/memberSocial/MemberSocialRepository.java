package com.auction.bid.domain.memberSocial;

import com.auction.bid.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberSocialRepository extends JpaRepository<MemberSocial, Long> {
    boolean existsByMemberIdAndProvider(Long memberId, SocialProvider provider);
    Optional<MemberSocial> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
}
