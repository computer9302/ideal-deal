package com.auction.bid.domain.memberAddress;

import com.auction.bid.domain.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "member_address",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_member_default_onlyone", columnNames = {"member_id", "default_flag"}),
                @UniqueConstraint(name = "uq_member_address_unique", columnNames = {"member_id", "city", "street", "zipcode"})
        })
@Getter
@NoArgsConstructor(access = PROTECTED)
@Builder
@AllArgsConstructor(access = PROTECTED)
public class MemberAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_member_address_member"))
    private Member member;

    @Column(name = "city", length = 255)
    private String city;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "zipcode", nullable = false)
    private String zipcode;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "default_flag")
    private Integer defaultFlag;

    void setMember(Member m) {
        this.member = m;
    }

    @PrePersist
    @PreUpdate
    private void syncDefaultFlag() {
        this.defaultFlag = this.isDefault ? 1 : null;
    }
}
