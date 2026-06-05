package com.auction.bid.domain.memberAddress;

import com.auction.bid.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {

    // 특정 회원의 기본 주소 1개
    Optional<MemberAddress> findByMemberIdAndIsDefaultTrue(Long memberId);

    // 특정 회원의 모든 주소
    List<MemberAddress> findAllByMember_Id(Long memberId);

    @Override
    <S extends MemberAddress> S save(S entity);
}
