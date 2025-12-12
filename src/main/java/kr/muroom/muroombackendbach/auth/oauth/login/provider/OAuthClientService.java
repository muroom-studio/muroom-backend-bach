package kr.muroom.muroombackendbach.auth.oauth.login.provider;

import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;

public interface OAuthClientService {

  OAuthProvider getProvider();

  /**
   * 인가 코드 → 외부 토큰 응답
   */
  OAuthTokenResult exchangeCode(String authorizationCode, String origin);

  /**
   * 외부 토큰에서 소셜 고유 유저 ID 추출 (sub, id 등)
   */
  String extractProviderUserId(OAuthTokenResult tokenResult);
}
