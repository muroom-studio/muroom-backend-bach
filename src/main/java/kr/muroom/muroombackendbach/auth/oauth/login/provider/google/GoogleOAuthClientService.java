package kr.muroom.muroombackendbach.auth.oauth.login.provider.google;

import static kr.muroom.muroombackendbach.auth.oauth.login.exception.OAuthLoginErrorCode.PROVIDER_USER_TOKEN_NOT_FOUND;

import com.auth0.jwt.interfaces.DecodedJWT;
import kr.muroom.muroombackendbach.auth.auth.domain.entity.OAuthProvider;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.OAuthClientService;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.OAuthTokenResult;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.google.dto.GoogleIdTokenPayload;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.google.dto.GoogleTokenResponse;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
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
      throw new BusinessException(PROVIDER_USER_TOKEN_NOT_FOUND);
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
