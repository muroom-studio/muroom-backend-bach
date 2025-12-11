package kr.muroom.muroombackendbach.auth.oauth.login.provider.kakao;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.OAuthClientService;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.OAuthTokenResult;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.kakao.dto.KakaoIdTokenPayload;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.kakao.dto.KakaoTokenResponse;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClientService implements OAuthClientService {

  private final KakaoOAuthClient kakaoOAuthClient;

  @Override
  public OAuthProvider getProvider() {
    return OAuthProvider.KAKAO;
  }

  @Override
  public OAuthTokenResult exchangeCode(String authorizationCode, String origin) {
    KakaoTokenResponse response =
        kakaoOAuthClient.exchangeCodeForToken(authorizationCode, origin);

    return OAuthTokenResult.builder()
        .accessToken(response.getAccessToken())
        .refreshToken(response.getRefreshToken())
        .accessTokenExpiresIn(response.getExpiresIn())
        .refreshTokenExpiresIn(response.getRefreshTokenExpiresIn())
        .idToken(response.getIdToken())
        .build();
  }

  @Override
  public String extractProviderUserId(OAuthTokenResult tokenResult) {
    if (tokenResult.idToken() == null) {
      throw new IllegalStateException("카카오 ID Token 이 존재하지 않습니다.");
    }
    KakaoIdTokenPayload payload = decode(tokenResult.idToken());
    return payload.getSub();
  }

  private KakaoIdTokenPayload decode(String idToken) {
    DecodedJWT jwt = JWT.decode(idToken);

    KakaoIdTokenPayload payload = new KakaoIdTokenPayload();
    payload.setAud(jwt.getClaim("aud").asString());
    payload.setSub(jwt.getSubject());
    payload.setAuthTime(jwt.getClaim("auth_time").asLong());
    payload.setIss(jwt.getIssuer());
    payload.setExp(jwt.getExpiresAt().getTime());
    payload.setIat(jwt.getIssuedAt().getTime());
    payload.setNickname(jwt.getClaim("nickname").asString());
    payload.setPicture(jwt.getClaim("picture").asString());
    payload.setEmail(jwt.getClaim("email").asString());

    return payload;
  }
}
