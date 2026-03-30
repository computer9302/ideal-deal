package com.auction.bid.domain.memberSocial;

import com.auction.bid.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "member_social",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_member_provider", columnNames = {"member_id", "provider"}),
                @UniqueConstraint(name = "uq_provider_uid", columnNames = {"provider", "provider_user_id"})
        },
        indexes = {
                @Index(name = "idx_member_provider", columnList = "member_id, provider"),
                @Index(name = "idx_provider_uid", columnList = "provider, provider_user_id")
        }

)
public class MemberSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_social_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_member_social_member")
    )
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private SocialProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId; // 공급자 측 유저 고유 ID (Google=sub, KaKao=id, Naver=id)

    @Builder
    private MemberSocial(Member member, SocialProvider provider, String providerUserId){
        if (member == null) throw new IllegalArgumentException("member required");
        if (provider == null) throw new IllegalArgumentException("provider required");
        if (providerUserId == null || providerUserId.isBlank())
            throw new IllegalArgumentException("providerUserId required");
        this.member = member;
        this.provider = provider;
        this.providerUserId = providerUserId.trim();
    }

    public static MemberSocial of(Member member, SocialProvider provider, String providerUserId){
        return MemberSocial.builder()
                .member(member)
                .provider(provider)
                .providerUserId(providerUserId)
                .build();
    }
}
