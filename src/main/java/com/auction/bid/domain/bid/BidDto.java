package com.auction.bid.domain.bid;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BidDto {

    private Long auctionId;
    private Long memberId;
    private String nickname;
    private Long bidAmount;
    private LocalDateTime bidTime;

    public static BidDto emptyDtoList(Long auctionId) {
        return BidDto.builder()
                .auctionId(auctionId)
                .memberId(null)
                .nickname(null)
                .bidAmount(null)
                .bidTime(null)
                .build();
    }

    public static Bid toBidEntity(BidDto bidDto, Member member, Auction auction) {
        return Bid.builder()
                .member(member)
                .auction(auction)
                .bidAmount(bidDto.getBidAmount())
                .bidTime(bidDto.getBidTime())
                .build();
    }

    public static List<BidDto> convertToBidDtoList(List<BidDto> bidDtoList) {
        List<BidDto> resultList = new ArrayList<>();
        if (bidDtoList == null) {
            return resultList;
        }

        for (Object bidData : bidDtoList) {
            LinkedHashMap<String, Object> bidMap = (LinkedHashMap<String, Object>) bidData;

            Long auctionId = ((Integer) bidMap.get("auctionId")).longValue();
            Long memberId = ((Integer) bidMap.get("memberId")).longValue();
            String nickname = (String) bidMap.get("nickname");
            Long bidAmount = ((Integer) bidMap.get("bidAmount")).longValue();
            LocalDateTime bidTime = formatTime((ArrayList<Integer>) bidMap.get("bidTime"));

            resultList.add(bidDtoBuild(auctionId, memberId, nickname, bidAmount, bidTime));
        }

        return resultList;
    }

    private static LocalDateTime formatTime(ArrayList<Integer> bidTimeList) {
        String bidTimeStr = String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d",
                bidTimeList.get(0),
                bidTimeList.get(1),
                bidTimeList.get(2),
                bidTimeList.get(3),
                bidTimeList.get(4),
                bidTimeList.get(5),
                bidTimeList.get(6));

        if (bidTimeStr.length() > 23) {
            bidTimeStr = bidTimeStr.substring(0, 23);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.parse(bidTimeStr, formatter);
    }

    private static BidDto bidDtoBuild(Long auctionId, Long memberId, String nickname, Long bidAmount, LocalDateTime bidTime) {
        return BidDto.builder()
                .auctionId(auctionId)
                .memberId(memberId)
                .nickname(nickname)
                .bidAmount(bidAmount)
                .bidTime(bidTime)
                .build();
    }
}
