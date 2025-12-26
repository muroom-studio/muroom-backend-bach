package kr.muroom.muroombackendbach.auth.oauth.login.provider.google;

import kr.muroom.muroombackendbach.auth.oauth.login.provider.OAuthClientService;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.OAuthTokenResult;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.google.dto.GoogleTokenResponse;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleOAuthClientService implements OAuthClientService {

  private final GoogleOAuthClient googleOAuthClient;

  @Override
  public OAuthProvider getProvider() {
    return OAuthProvider.GOOGLE;
  }

  @Override
  public OAuthTokenResult exchangeCode(String authorizationCode, String origin) {
    GoogleTokenResponse response = googleOAuthClient.exchangeCodeForToken(
        authorizationCode);

    return OAuthTokenResult.builder()
        .idToken(response.getSub())
        .build();
  }

  @Override
  public String extractProviderUserId(OAuthTokenResult tokenResult) {
    return tokenResult.idToken();
  }
}
