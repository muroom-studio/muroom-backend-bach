package kr.muroom.muroombackendbach.auth.jwt;

import static kr.muroom.muroombackendbach.auth.jwt.exception.JwtErrorCode.INVALID_SIGNUP_TOKEN;
import static kr.muroom.muroombackendbach.auth.jwt.exception.JwtErrorCode.MISSING_SIGNUP_TOKEN_CLAIMS;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import kr.muroom.muroombackendbach.auth.auth.domain.entity.UserType;
import kr.muroom.muroombackendbach.auth.jwt.exception.JwtErrorCode;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  private final SecretKey key;

  private final long accessExpirationMs;
  private final long refreshExpirationMs;
  private final long signupExpirationMs;

  public JwtTokenProvider(
      @Value("${jwt.secret-key}") String secret,
      @Value("${jwt.access-expiration-ms}") long accessExpirationMs,
      @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs,
      @Value("${jwt.signup-expiration-ms:600000}") long signupExpirationMs // 없으면 10분 기본
  ) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.accessExpirationMs = accessExpirationMs;
    this.refreshExpirationMs = refreshExpirationMs;
    this.signupExpirationMs = signupExpirationMs;
  }

  // =========================
  // Token Create
  // =========================

  /**
   * 로그인 후 사용하는 accessToken (musicianId 기반)
   */
  public String createAccessToken(UserType userType, Long actorId) {
    Claims claims = Jwts.claims()
        .subject(String.valueOf(actorId))
        .add("type", "ACCESS")
        .add("userType", userType.name())
        .build();

    return buildToken(claims, accessExpirationMs);
  }

  /**
   * refreshToken 생성 - jti: refresh 토큰 식별자(저장소 키로 사용) - (권장) refresh는 서버 저장소에 jti를 저장/회전시켜야 진짜 의미가
   * 생김
   */
  public RefreshIssue createRefreshToken(UserType userType, Long actorId) {
    String jti = UUID.randomUUID().toString();

    Claims claims = Jwts.claims()
        .subject(String.valueOf(actorId))
        .add("type", "REFRESH")
        .add("userType", userType.name())
        .add("jti", jti)
        .build();

    String token = buildToken(claims, refreshExpirationMs);
    Date expiresAt = new Date(System.currentTimeMillis() + refreshExpirationMs);

    return new RefreshIssue(token, jti, expiresAt);
  }

  /**
   * 회원가입 단계에서 사용하는 signupToken (provider, providerId 기반)
   */
  public String createSignupToken(String provider, String providerId) {
    Claims claims = Jwts.claims()
        .add("type", "SIGNUP")
        .add("provider", provider)
        .add("providerId", providerId)
        .build();

    return buildToken(claims, signupExpirationMs);
  }

  /**
   * 휴대폰 인증 완료 후 회원가입에서 사용하는 phoneVerifyToken - phoneNumber 기반 - jti 포함(1회 사용 처리용)
   */
  public PhoneVerifyIssue createPhoneVerifyToken(String phoneNumber) {
    String jti = UUID.randomUUID().toString();

    Claims claims = Jwts.claims()
        .add("type", "PHONE_VERIFY")
        .add("phoneNumber", phoneNumber)
        .add("jti", jti)
        .build();

    String token = buildToken(claims, signupExpirationMs);
    Date expiresAt = new Date(System.currentTimeMillis() + signupExpirationMs);

    return new PhoneVerifyIssue(token, jti, expiresAt);
  }

  private String buildToken(Claims claims, long ttlMs) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + ttlMs);

    return Jwts.builder()
        .claims(claims)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  // =========================
  // Parse / Extract
  // =========================

  /**
   * accessToken에서 musicianId 추출
   */
  public Long getMusicianIdFromAccess(String token) {
    Claims claims = parseClaims(token);
    // (선택) type 검증
    if (!"ACCESS".equals(claims.get("type"))) {
      throw new JwtException("Not an ACCESS token");
    }
    return Long.valueOf(claims.getSubject());
  }

  /**
   * refreshToken에서 payload 추출 (musicianId, jti)
   */
  public RefreshPayload parseRefreshToken(String refreshToken) {
    Claims claims = parseClaims(refreshToken);

    Object type = claims.get("type");
    if (!"REFRESH".equals(type)) {
      throw new JwtException("Not a REFRESH token");
    }

    String sub = claims.getSubject();
    String jti = (String) claims.get("jti");
    if (sub == null || jti == null) {
      throw new JwtException("Missing refresh claims");
    }

    return new RefreshPayload(UserType.MUSICIAN, Long.valueOf(sub), jti);
  }

  /**
   * phoneVerifyToken에서 payload 추출 (phoneNumber, jti)
   */
  public PhoneVerifyPayload parsePhoneVerifyToken(String phoneVerifyToken) {
    Claims claims = parseClaims(phoneVerifyToken);

    Object type = claims.get("type");
    if (!"PHONE_VERIFY".equals(type)) {
      throw new BusinessException(JwtErrorCode.INVALID_SMS_VERIFY_TOKEN);
    }

    String phoneNumber = (String) claims.get("phoneNumber");
    String jti = (String) claims.get("jti");

    if (phoneNumber == null || jti == null) {
      throw new BusinessException(JwtErrorCode.INVALID_SMS_VERIFY_TOKEN);
    }

    return new PhoneVerifyPayload(phoneNumber, jti);
  }

  /**
   * signupToken에서 provider / providerId 추출
   */
  public SignupPayload parseSignupToken(String signupToken) {
    Claims claims = parseClaims(signupToken);

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

  // =========================
  // Validate / Parse
  // =========================

  /**
   * 공용 토큰 검증 (exp, sig 등)
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

  private Claims parseClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (ExpiredJwtException e) {
      log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
      throw e;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
      throw e;
    }
  }

  // =========================
  // Records
  // =========================

  /**
   * signupToken에서 꺼낸 값 전달용
   */
  public record SignupPayload(String provider, String providerId) {

  }

  /**
   * refreshToken에서 꺼낸 값 전달용
   */
  public record RefreshPayload(UserType userType, Long userId, String jti) {

  }

  /**
   * refresh 발급 결과(토큰, jti, 만료시각)
   */
  public record RefreshIssue(String token, String jti, Date expiresAt) {

  }

  /**
   * phoneVerifyToken에서 꺼낸 값 전달용
   */
  public record PhoneVerifyPayload(String phoneNumber, String jti) {

  }

  /**
   * phoneVerifyToken 발급 결과(토큰, jti, 만료시각)
   */
  public record PhoneVerifyIssue(String token, String jti, Date expiresAt) {

  }
}
