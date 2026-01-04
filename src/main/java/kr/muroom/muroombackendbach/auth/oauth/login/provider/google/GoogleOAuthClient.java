package kr.muroom.muroombackendbach.auth.oauth.login.provider.google;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.google.dto.GoogleTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthClient {

  @Value("${oauth2.google.client-id}")
  private String clientId;

  @Value("${oauth2.google.client-secret}")
  private String clientSecret;

  // 카카오처럼 origin 기반으로 redirect_uri를 만들 거면 동일한 패턴으로 두는 게 안전함
  private static final String REDIRECT_URI = "/redirect/oauth/google";
  private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";

  private final RestTemplate restTemplate = new RestTemplate();

  public GoogleTokenResponse exchangeCodeForToken(String authorizationCode, String origin) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    String decodedCode = URLDecoder.decode(authorizationCode, StandardCharsets.UTF_8);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);

    body.add("redirect_uri", origin + REDIRECT_URI);
    body.add("code", decodedCode);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

    ResponseEntity<GoogleTokenResponse> response =
        restTemplate.postForEntity(TOKEN_URI, request, GoogleTokenResponse.class);

    log.info("[Google OAuth] token response={}", response.getBody());

    return response.getBody();
  }
}
