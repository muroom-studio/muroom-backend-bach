package kr.muroom.muroombackendbach.user.domain.entity;

import static kr.muroom.muroombackendbach.auth.oauth.login.exception.OAuthLoginErrorCode.*;

import kr.muroom.muroombackendbach.common.exception.BusinessException;

public enum OAuthProvider {
  KAKAO, NAVER, GOOGLE;

  public static OAuthProvider fromRegistrationId(String registrationId) {
    return switch (registrationId.toUpperCase()) {
      case "KAKAO" -> KAKAO;
      case "GOOGLE" -> GOOGLE;
      case "NAVER" -> NAVER;
      default -> throw new BusinessException(UNSUPPORTED_OAUTH_PROVIDER);
    };
  }
}
