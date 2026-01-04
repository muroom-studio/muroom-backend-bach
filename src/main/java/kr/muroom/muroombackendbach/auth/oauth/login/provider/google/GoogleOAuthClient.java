package kr.muroom.muroombackendbach.auth.oauth.login.provider.google;

import static kr.muroom.muroombackendbach.auth.oauth.login.exception.OAuthLoginErrorCode.FAIL_MAKE_PUBLIC_KEY;
import static kr.muroom.muroombackendbach.auth.oauth.login.exception.OAuthLoginErrorCode.PROVIDER_INVALID_RESPONSE;
import static kr.muroom.muroombackendbach.auth.oauth.login.exception.OAuthLoginErrorCode.PROVIDER_NOT_RESPONSE;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import kr.muroom.muroombackendbach.auth.auth.exception.AuthErrorCode;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.google.dto.GoogleTokenResponse;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
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

  private static final String REDIRECT_URI = "/redirect/oauth/google";
  private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";

  // ✅ Google JWKS (공개키) 엔드포인트
  private static final String CERTS_URI = "https://www.googleapis.com/oauth2/v3/certs";

  private final RestTemplate restTemplate = new RestTemplate();

  public GoogleTokenResponse exchangeCodeForToken(String authorizationCode, String origin) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    // 프론트에서 %2F 형태로 넘어오면 디코딩해서 원본 code로 교환
    String decodedCode = URLDecoder.decode(authorizationCode, StandardCharsets.UTF_8);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    body.add("redirect_uri", origin + REDIRECT_URI);
    body.add("code", decodedCode);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<GoogleTokenResponse> response =
          restTemplate.postForEntity(TOKEN_URI, request, GoogleTokenResponse.class);
      return response.getBody();
    } catch (Exception e) {
      throw new BusinessException(AuthErrorCode.LOGIN_FAIL);
    }
  }

  /**
   * Google ID Token 서명 검증 + iss/aud 검증 - JWT.decode()로 kid 추출 후, Google certs(JWKS)에서 kid에 해당하는
   * 공개키를 찾아 검증 - 검증 실패 시 예외(com.auth0.jwt.exceptions.JWTVerificationException) 발생
   */
  public DecodedJWT verifyIdToken(String idToken) {
    DecodedJWT decoded = JWT.decode(idToken);
    String kid = decoded.getKeyId();
    if (kid == null || kid.isBlank()) {
      throw new BusinessException(AuthErrorCode.LOGIN_FAIL);
    }

    // 2) kid에 해당하는 RSAPublicKey 가져오기
    RSAPublicKey publicKey = getGooglePublicKey(kid);

    // 3) 서명 및 클레임 검증
    Algorithm algorithm = Algorithm.RSA256(publicKey, null);

    JWTVerifier verifier = JWT.require(algorithm)
        .withAudience(clientId)
        .withIssuer("https://accounts.google.com", "accounts.google.com")
        .build();

    return verifier.verify(idToken);
  }

  @SuppressWarnings("unchecked")
  private RSAPublicKey getGooglePublicKey(String kid) {
    Map<String, Object> jwks = restTemplate.getForObject(CERTS_URI, Map.class);
    if (jwks == null || !jwks.containsKey("keys")) {
      throw new BusinessException(PROVIDER_NOT_RESPONSE);
    }

    List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");

    Map<String, Object> matched = keys.stream()
        .filter(k -> kid.equals(String.valueOf(k.get("kid"))))
        .findFirst()
        .orElseThrow(
            () -> new BusinessException(PROVIDER_INVALID_RESPONSE));

    String n = String.valueOf(matched.get("n"));
    String e = String.valueOf(matched.get("e"));

    return buildRsaPublicKey(n, e);
  }

  private RSAPublicKey buildRsaPublicKey(String n, String e) {
    try {
      BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
      BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));

      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return (RSAPublicKey) keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent));
    } catch (Exception ex) {
      throw new BusinessException(FAIL_MAKE_PUBLIC_KEY);
    }
  }
}
