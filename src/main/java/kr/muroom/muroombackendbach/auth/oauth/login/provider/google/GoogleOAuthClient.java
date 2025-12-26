package kr.muroom.muroombackendbach.auth.oauth.login.provider.google;

import kr.muroom.muroombackendbach.auth.oauth.login.provider.google.dto.GoogleTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthClient {

  private static final String USERINFO_URI = "https://openidconnect.googleapis.com/v1/userinfo";

  private final RestTemplate restTemplate = new RestTemplate();

  public GoogleTokenResponse exchangeCodeForToken(String accessToken) {
    if (accessToken == null || accessToken.isBlank()) {
      throw new IllegalArgumentException("Google AccessToken이 비어있습니다.");
    }
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);

      HttpEntity<Void> request = new HttpEntity<>(headers);

      ResponseEntity<GoogleTokenResponse> response =
          restTemplate.exchange(USERINFO_URI, HttpMethod.GET, request, GoogleTokenResponse.class);

      return response.getBody();

    } catch (HttpStatusCodeException e) {
      log.warn("[Google userinfo 실패] status={}, body={}", e.getStatusCode(),
          e.getResponseBodyAsString());
      throw new IllegalStateException("Google userinfo 요청에 실패했습니다. status=" + e.getStatusCode(), e);

    } catch (RestClientException e) {
      log.warn("[Google userinfo 실패] RestClientException", e);
      throw new IllegalStateException("Google userinfo 요청 중 오류가 발생했습니다.", e);
    }
  }
}
