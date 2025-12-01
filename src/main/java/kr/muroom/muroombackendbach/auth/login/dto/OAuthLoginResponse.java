package kr.muroom.muroombackendbach.auth.login.dto;

import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;

public record OAuthLoginResponse(
    ResultType type,
    String accessToken,
    String signupToken,
    Long userId,
    OAuthProvider provider
) {

  public enum ResultType {
    LOGIN,
    SIGNUP_REQUIRED
  }

  // 로그인 성공용 팩토리 메서드
  public static OAuthLoginResponse login(String accessToken, Long userId, OAuthProvider provider) {
    return new OAuthLoginResponse(ResultType.LOGIN, accessToken, null, userId, provider);
  }

  // 회원가입 필요용 팩토리 메서드
  public static OAuthLoginResponse signupRequired(String signupToken, OAuthProvider provider) {
    return new OAuthLoginResponse(ResultType.SIGNUP_REQUIRED, null, signupToken, null, provider);
  }
}