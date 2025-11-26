package kr.muroom.muroombackendbach.user.domain.entity;

public enum OAuthProvider {
    KAKAO, NAVER, GOOGLE;

    public static OAuthProvider fromRegistrationId(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "kakao" -> KAKAO;
            case "google" -> GOOGLE;
            case "naver" -> NAVER;
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }
}
