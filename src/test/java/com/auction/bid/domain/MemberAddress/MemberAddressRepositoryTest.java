package com.auction.bid.domain.MemberAddress;

import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.memberAddress.MemberAddress;
import com.auction.bid.domain.memberAddress.MemberAddressRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class MemberAddressRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberAddressRepository addressRepository;

    @Test
    void findByMemberIdAndIsDefaultTrue() {
        // given
        Member m = memberRepository.save(Member.builder()
                        .email("a@a.com")
                        .password("pw")
                        .build());

        addressRepository.save(MemberAddress.builder()
                        .member(m)
                        .city("Seoul")
                        .street("101")
                        .zipcode("00000")
                .build());

        MemberAddress defaultAddr = addressRepository.save(MemberAddress.builder()
                        .member(m)
                        .city("Seoul")
                        .street("102")
                        .zipcode("00001")
                        .isDefault(true)
                .build());

        // when
        Optional<MemberAddress> found =
                addressRepository.findByMemberIdAndIsDefaultTrue(m.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(defaultAddr.getId());
    }

    @Test
    void findAllByMember_id() {
    }
}