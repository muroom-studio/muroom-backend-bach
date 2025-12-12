package kr.muroom.muroombackendbach.auth.jwt;

import static kr.muroom.muroombackendbach.auth.jwt.exception.JwtErrorCode.INVALID_SIGNUP_TOKEN;
import static kr.muroom.muroombackendbach.auth.jwt.exception.JwtErrorCode.MISSING_SIGNUP_TOKEN_CLAIMS;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  private final SecretKey key;
  private final long expiration;

  public JwtTokenProvider(
      @Value("${jwt.secret-key}") String secret,
      @Value("${jwt.expiration-ms}") long expiration
  ) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.expiration = expiration;
  }

  /**
   * 로그인 후 사용하는 accessToken (musicianId 기반)
   */
  public String createToken(Long musicianId) {
    Claims claims = Jwts.claims().subject(String.valueOf(musicianId)).build();
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .claims(claims)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  /**
   * 회원가입 단계에서 사용하는 signupToken (provider, providerId 기반)
   */
  public String createSignupToken(String provider, String providerId) {
    Claims claims = Jwts.claims()
        .add("type", "SIGNUP")        // 토큰 용도 구분(선택)
        .add("provider", provider)
        .add("providerId", providerId)
        .build();

    Date now = new Date();
    Date expiry = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .claims(claims)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  /**
   * accessToken에서 musicianId 추출
   */
  public Long getMusicianId(String token) {
    Claims claims = parseClaims(token);
    return Long.valueOf(claims.getSubject());
  }

  /**
   * signupToken에서 provider / providerId 추출
   */
  public SignupPayload parseSignupToken(String signupToken) {
    Claims claims = parseClaims(signupToken);

    // 선택: type 검증 (SIGNUP 토큰인지 체크)
    Object type = claims.get("type");
    if (!"SIGNUP".equals(type)) {
      throw new BusinessException(INVALID_SIGNUP_TOKEN);
    }

    String provider = (String) claims.get("provider");
    String providerId = (String) claims.get("providerId");

    if (provider == null || providerId == null) {
      throw new BusinessException(MISSING_SIGNUP_TOKEN_CLAIMS);
    }

    return new SignupPayload(provider, providerId);
  }

  /**
   * 공용 토큰 검증 (accessToken / signupToken 둘 다 사용 가능)
   */
  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (ExpiredJwtException e) {
      log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
    }
    return false;
  }

  /*** 내부용 Claims 파싱
   */

  private Claims parseClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(key)    // 서명 검증
          .build()
          .parseSignedClaims(token)  // 여기서 자동으로 exp 등 검증됨
          .getPayload();
    } catch (ExpiredJwtException e) {
      log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
      throw e;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * signupToken에서 꺼낸 값 전달용 record
   */
  public record SignupPayload(String provider, String providerId) {

  }
}
