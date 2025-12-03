package kr.muroom.muroombackendbach.auth.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

  @Value("${oauth2.kakao.client-id}")
  private String clientId;

  @Value("${oauth2.kakao.client-secret}")
  private String clientSecret;

  private final RestTemplate restTemplate = new RestTemplate();

  public KakaoTokenResponse exchangeCodeForToken(String authorizationCode, String redirectUri) {
    String url = "https://kauth.kakao.com/oauth/token";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("client_id", clientId);
    body.add("redirect_uri", redirectUri);
    body.add("code", authorizationCode);

    if (clientSecret != null && !clientSecret.isBlank()) {
      body.add("client_secret", clientSecret);
    }

    HttpEntity<MultiValueMap<String, String>> request =
        new HttpEntity<>(body, headers);

    ResponseEntity<KakaoTokenResponse> response =
        restTemplate.postForEntity(url, request, KakaoTokenResponse.class);

    return response.getBody();
  }
}
