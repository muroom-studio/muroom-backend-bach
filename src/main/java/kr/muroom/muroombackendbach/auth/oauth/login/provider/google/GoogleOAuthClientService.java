package kr.muroom.muroombackendbach.auth.oauth.login.provider.google;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.OAuthClientService;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.OAuthTokenResult;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.google.dto.GoogleIdTokenPayload;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.google.dto.GoogleTokenResponse;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthClientService implements OAuthClientService {

  private final GoogleOAuthClient googleOAuthClient;

  @Override
  public OAuthProvider getProvider() {
    return OAuthProvider.GOOGLE;
  }

  @Override
  public OAuthTokenResult exchangeCode(String authorizationCode, String origin) {
    GoogleTokenResponse response =
        googleOAuthClient.exchangeCodeForToken(authorizationCode, origin);

    return OAuthTokenResult.builder()
        .idToken(response.idToken())
        .build();
  }

  @Override
  public String extractProviderUserId(OAuthTokenResult tokenResult) {
    if (tokenResult.idToken() == null) {
      throw new IllegalStateException("구글 ID Token 이 존재하지 않습니다.");
    }

    DecodedJWT verifiedJwt = googleOAuthClient.verifyIdToken(tokenResult.idToken());

    GoogleIdTokenPayload payload = toPayload(verifiedJwt);
    return payload.getSub();
  }

  private GoogleIdTokenPayload toPayload(DecodedJWT jwt) {
    GoogleIdTokenPayload payload = new GoogleIdTokenPayload();
    payload.setIss(jwt.getIssuer());
    payload.setAud(jwt.getClaim("aud").asString());
    payload.setSub(jwt.getSubject());
    payload.setEmail(jwt.getClaim("email").asString());
    payload.setEmailVerified(jwt.getClaim("email_verified").asBoolean());
    payload.setName(jwt.getClaim("name").asString());
    payload.setPicture(jwt.getClaim("picture").asString());

    if (jwt.getExpiresAt() != null) {
      payload.setExp(jwt.getExpiresAt().getTime());
    }
    if (jwt.getIssuedAt() != null) {
      payload.setIat(jwt.getIssuedAt().getTime());
    }

    return payload;
  }
}
