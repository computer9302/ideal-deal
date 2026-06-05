package com.auction.bid.domain.member;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionRepository;
import com.auction.bid.domain.auction.AuctionStatus;
import com.auction.bid.domain.bid.Bid;
import com.auction.bid.domain.member.dto.*;
import com.auction.bid.domain.memberAddress.MemberAddress;
import com.auction.bid.domain.memberAddress.MemberAddressRepository;
import com.auction.bid.domain.sale.Sale;
import com.auction.bid.domain.sale.SaleStatus;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.AuthException;
import com.auction.bid.global.exception.exceptions.MailException;
import com.auction.bid.global.exception.exceptions.MemberException;
import com.auction.bid.global.exception.exceptions.MoneyException;
import com.auction.bid.global.querydsl.QueryDslRepository;
import com.auction.bid.global.security.ConstSecurity;
import com.auction.bid.global.security.RefreshTokenRepository;
import com.auction.bid.global.security.jwt.JWTUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JavaMailSender mailSender;
    private final JWTUtil jwtUtil;
    @Qualifier("productRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final RefreshTokenRepository refreshTokenRepository;
    private final QueryDslRepository queryDslRepository;
    private final MemberAddressRepository memberAddressRepository;
    private final AuctionRepository auctionRepository;

    /**
     * 회원가입 처리
     * @param request 회원가입 요청 DTO
     * @return 회원가입 응답 DTO
     */
    @Override
    public SignUpDto.Response signUp(SignUpDto.Request request) {
        if (request.getEmailVerified() == null || !request.getEmailVerified()) {
            throw new MailException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (memberRepository.existsByLoginId(request.getLoginId())) {
            throw new MemberException(ErrorCode.ALREADY_EXIST_LOGIN_ID);
        }

        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());
        Member savedMember = memberRepository.save(SignUpDto.Request.toEntity(request, encodedPassword));

        if (request.getAddressRequest() != null){
            MemberAddress address = SignUpDto.Request.toAddressEntity(request, savedMember, true);
            memberAddressRepository.save(address);
        }
        return SignUpDto.Response.fromEntity(savedMember);
    }

    /**
     * 이메일 전송
     * @param to 수신 이메일 주소
     * @return 이메일 주소
     */
    @Override
    @Qualifier("productRedisTemplate")
    public String sendEmail(String to) {
        MimeMessage message = mailSender.createMimeMessage();
        String token = UUID.randomUUID().toString().substring(0, 6);

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("메일 인증코드");
            String emailContent = "<br>회원가입을 위한 메일 인증코드입니다.<p>" +
                    "<p>" + token + "</p>" +
                    "<p>만료시간은 10분입니다.</p>";

            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailException(ErrorCode.CAN_NOT_SEND_MAIL);
        }
        redisTemplate.opsForValue().set(to, token, 10, TimeUnit.MINUTES);

        return to;
    }

    /**
     * 이메일 인증 처리
     * @param email 이메일 주소
     * @param token 인증 토큰
     * @return 인증 성공 여부
     */
    @Override
    @Qualifier("productRedisTemplate")
    public boolean verifyEmail(String email, String token) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(email))) {
            throw new MailException(ErrorCode.TOKEN_NOT_FOUND);
        }

        if (!Objects.equals(redisTemplate.opsForValue().get(email), token)) {
            throw new MemberException(ErrorCode.INVALID_TOKEN);
        }

        redisTemplate.delete(email);
        return true;
    }

    /**
     * 로그아웃 처리
     * @param token 인증 토큰
     * @return 멤버 UUID
     */
    @Override
    @Qualifier("productRedisTemplate")
    public String logout(String token) {
        if (token == null || !token.startsWith(ConstSecurity.BEARER)) {
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        }

        String jwtToken = jwtUtil.getTokenFromHeader(token);
        String memberUUID = jwtUtil.getMemberStrUUIDFromToken(jwtToken);
        redisTemplate.opsForValue().set(jwtToken, ConstSecurity.BLACK_LIST, 1, TimeUnit.DAYS);
        refreshTokenRepository.deleteByMemberId(UUID.fromString(memberUUID));

        return memberUUID;
    }

    /**
     * 멤버 조회
     * @param memberUUID 멤버 UUID
     * @return 멤버 엔티티
     */
    @Override
    public Member findByMemberUUID(UUID memberUUID) {
        return memberRepository.findByMemberUUID(memberUUID)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));
    }

    /**
     * 잔액 추가
     * @param memberId 멤버 ID
     * @param amount 추가 금액
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addMoney(Long memberId, Long amount) {
        Member findMember = memberRepository.lockMemberForUpdate(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));

        findMember.addBalance(amount);
    }

    /**
     * 충전 요청 처리
     * @param token 사용자 인증 토큰
     * @param dtoRequest 충전 요청 DTO
     * @return 충전 후 잔액 응답 DTO
     * @throws AuthException 인증 토큰이 유효하지 않을 경우 발생
     */
    @Override
    public ChargeDto.Response chargeMoney(String token, ChargeDto.Request dtoRequest) {
        if (token == null || !token.startsWith(ConstSecurity.BEARER)) {
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        }
        Member findMember = getMemberFromToken(token);
        Long currMoney = findMember.addBalance(dtoRequest.getChargeMoney());
        return new ChargeDto.Response(currMoney);
    }

    /**
     * 사용자 잔액 조회
     * @param token 사용자 인증 토큰
     * @return 사용자의 현재 잔액
     */
    @Override
    public Long getMoney(String token) {
        Member findMember = getMemberFromToken(token);
        return findMember.getBalance();
    }

    /**
     * 경매 입찰 처리
     * @param member 입찰을 수행하는 멤버 객체
     * @param bidMoney 입찰 금액
     * @param lastMoney 이전 최고 입찰 금액
     * @throws MoneyException 사용자의 잔액이 부족할 경우 발생
     */
    @Override
    public void bidToAuction(Member member, Long bidMoney, Long lastMoney) {
        Long balance = member.getBalance();
        long neededMoney = bidMoney - lastMoney;
        if (balance - neededMoney < 0) {
            throw new MoneyException(ErrorCode.NOT_ENOUGH_MONEY);
        }

        member.subBalance(neededMoney);
        memberRepository.save(member);
    }

    /**
     * 잔액 출금 처리
     * @param memberId 멤버 ID
     * @param withDrawMoney 출금 금액
     * @throws MemberException 멤버가 존재하지 않을 경우 발생
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void withDraw(Long memberId, Long withDrawMoney) {
        Member findMember = memberRepository.lockMemberForUpdate(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));

        findMember.addBalance(withDrawMoney);
    }

    /**
     * 경매 기록 조회
     * @param token 사용자 인증 토큰
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param auctionStatus 경매 상태
     * @return 경매 기록의 페이징된 목록
     */
    @Override
    public Page<AuctionHistoryDto> getAuctionHistory(String token, int page, int size, AuctionStatus auctionStatus) {
        Pageable pageable = PageRequest.of(page, size);
        UUID memberUUID = jwtUtil.getMemberUUIDFromToken(token);
        Page<Auction> auctionPage = queryDslRepository.getAuctionList(memberUUID, pageable, auctionStatus);
        List<AuctionHistoryDto> auctionList = auctionPage.stream()
                .map(AuctionHistoryDto::fromAuction)
                .toList();

        return PageableExecutionUtils.getPage(auctionList, pageable, auctionPage::getTotalElements);
    }

    /**
     * 특정 경매 상세 정보 조회
     * @param token 사용자 인증 토큰
     * @param auctionId 경매 ID
     * @return 경매 상세 정보 DTO
     */
    @Override
    public DetailAuctionHistoryDto getAuctionDetail(String token, Long auctionId) {
        UUID memberUUID = jwtUtil.getMemberUUIDFromToken(token);
        Member findMember = memberRepository.findByMemberUUID(memberUUID)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));
        Auction findAuction = queryDslRepository.getAuctionEagerly(auctionId);
        List<BidHistoryDto> bidDtoList = getBidHistoryByProductId(findAuction.getProduct().getId());
        return DetailAuctionHistoryDto.fromAuction(findAuction, findMember.getId(), bidDtoList);
    }

    /**
     * 판매 기록 조회
     * @param token 사용자 인증 토큰
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param saleStatus 판매 상태
     * @return 판매 기록 DTO 목록
     */
    @Override
    public List<SaleHistoryDto> getSaleHistory(String token, int page, int size, SaleStatus saleStatus) {
        Pageable pageable = PageRequest.of(page, size);
        UUID memberUUID = jwtUtil.getMemberUUIDFromToken(token);
        List<Sale> saleList = queryDslRepository.getSaleList(memberUUID, pageable, saleStatus);
        return saleList.stream()
                .map(SaleHistoryDto::fromSale)
                .toList();
    }

    /**
     * 특정 판매 상세 정보 조회
     * @param saleId 판매 ID
     * @return 판매 상세 정보 DTO
     */
    @Override
    public DetailSaleHistoryDto getSaleDetail(Long saleId) {
        Sale findSale = queryDslRepository.getSaleEagerly(saleId);
        List<BidHistoryDto> bidDtoList = getBidHistoryByProductId(findSale.getProduct().getId());
        Auction auction = auctionRepository.findFirstByProductIdAndMemberIsNullOrderByIdAsc(findSale.getProduct().getId())
                .orElse(null);
        LocalDateTime auctionStart = auction != null ? auction.getAuctionStart() : null;
        LocalDateTime auctionEnd = auction != null ? auction.getAuctionEnd() : null;
        return DetailSaleHistoryDto.fromSale(findSale, auctionStart, auctionEnd, bidDtoList);
    }

    /**
     * 상품 ID로 입찰 기록 조회
     * @param productId 상품 ID
     * @return 입찰 기록 DTO 목록
     */
    private List<BidHistoryDto> getBidHistoryByProductId(Long productId) {
        List<Bid> bidList = queryDslRepository.findAllByAuctionProductId(productId);
        return bidList.stream()
                .map(BidHistoryDto::fromBidEntity)
                .toList();
    }

    /**
     * 토큰에서 멤버 정보 추출
     * @param token 사용자 인증 토큰
     * @return 멤버 엔티티
     * @throws MemberException 멤버가 존재하지 않을 경우 발생
     */
    private Member getMemberFromToken(String token) {
        UUID memberUUID = jwtUtil.getMemberUUIDFromToken(token);
        return memberRepository.findByMemberUUID(memberUUID)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));
    }

}
