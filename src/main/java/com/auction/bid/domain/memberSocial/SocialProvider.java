package com.auction.bid.domain.memberSocial;

public enum SocialProvider {
    GOOGLE("google"),
    KAKAO("kakao"),
    NAVER("naver");

    private final String registrationId;

    SocialProvider(String registrationId) {
        this.registrationId = registrationId;
    }

    public static SocialProvider from(String registrationId){
        for (SocialProvider p : values()){
            if (p.registrationId.equalsIgnoreCase(registrationId)){
                return p;
            }
        }
        throw new IllegalArgumentException("Unsupported provider: " + registrationId);
    }
}
