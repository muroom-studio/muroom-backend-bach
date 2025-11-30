package kr.muroom.muroombackendbach.user.domain.entity;

public enum OAuthProvider {
  KAKAO, NAVER, GOOGLE;

  public static OAuthProvider fromRegistrationId(String registrationId) {
    return switch (registrationId.toUpperCase()) {
      case "KAKAO" -> KAKAO;
      case "GOOGLE" -> GOOGLE;
      case "NAVER" -> NAVER;
      default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
    };
  }
}
