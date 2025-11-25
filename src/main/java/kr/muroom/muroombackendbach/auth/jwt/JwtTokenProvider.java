package kr.muroom.muroombackendbach.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long expiration;

    public JwtTokenProvider(
            @Value("${JWT_SECRET_KEY}") String secret,
            @Value("${JWT_EXP}") long expiration
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
            throw new IllegalArgumentException("유효하지 않은 회원가입 토큰입니다.");
        }

        String provider = (String) claims.get("provider");
        String providerId = (String) claims.get("providerId");

        if (provider == null || providerId == null) {
            throw new IllegalArgumentException("회원가입 토큰에 필수 정보가 없습니다.");
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
            System.out.println("JWT expired: " + e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("JWT invalid: " + e.getMessage());
        }
        return false;
    }

    /**
     * 내부용 Claims 파싱
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * signupToken에서 꺼낸 값 전달용 record
     */
    public record SignupPayload(String provider, String providerId) {}
}
