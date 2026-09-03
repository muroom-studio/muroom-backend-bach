package kr.muroom.muroombackendbach.auth.jwt;

import static kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider.RefreshIssue;
import static kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider.RefreshPayload;
import static kr.muroom.muroombackendbach.auth.jwt.exception.JwtErrorCode.EXPIRED_REFRESH_TOKEN;
import static kr.muroom.muroombackendbach.auth.jwt.exception.JwtErrorCode.INVALID_REFRESH_TOKEN;
import static kr.muroom.muroombackendbach.auth.jwt.exception.JwtErrorCode.REUSED_REFRESH_TOKEN;

import io.jsonwebtoken.ExpiredJwtException;
import java.time.Duration;
import java.util.Date;
import java.util.Set;
import kr.muroom.muroombackendbach.auth.auth.domain.entity.UserType;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final StringRedisTemplate redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;

  private static final String REFRESH_JTI_KEY_PREFIX = "refresh:";           // refresh:{jti}
  private static final String USER_JTI_SET_KEY_PREFIX = "refresh:user:";     // refresh:user
  // :{musicianId}

  public void save(Long musicianId, String jti, Date expiresAt) {
    String jtiKey = REFRESH_JTI_KEY_PREFIX + jti;
    String userSetKey = USER_JTI_SET_KEY_PREFIX + musicianId;

    long ttlMs = expiresAt.getTime() - System.currentTimeMillis();
    if (ttlMs <= 0) {
      return; // 이미 만료 시각이면 저장 안 함
    }

    // refresh:{jti} = musicianId (TTL 설정)
    redisTemplate.opsForValue().set(jtiKey, String.valueOf(musicianId), Duration.ofMillis(ttlMs));

    // revokeAll을 위해 유저별 jti set에 추가
    redisTemplate.opsForSet().add(userSetKey, jti);

    // userSetKey도 너무 오래 남지 않게 TTL을 refresh TTL과 비슷하게 맞춤(최소 ttlMs)
    // 기존 TTL이 더 길면 줄이지 않도록(선택) → 여기서는 단순히 ttlMs로 설정
    redisTemplate.expire(userSetKey, Duration.ofMillis(ttlMs));
  }

  public boolean isValid(UserType userType, Long userId, String jti) {
    String jtiKey = REFRESH_JTI_KEY_PREFIX + jti;
    String stored = redisTemplate.opsForValue().get(jtiKey);
    return stored != null && stored.equals(String.valueOf(userId));
  }

  public void revoke(Long musicianId, String jti) {
    String jtiKey = REFRESH_JTI_KEY_PREFIX + jti;
    String userSetKey = USER_JTI_SET_KEY_PREFIX + musicianId;

    // refresh:{jti} 삭제
    redisTemplate.delete(jtiKey);

    // 유저 set에서 jti 제거
    redisTemplate.opsForSet().remove(userSetKey, jti);
  }

  public void revokeAll(Long musicianId) {
    String userSetKey = USER_JTI_SET_KEY_PREFIX + musicianId;

    Set<String> jtis = redisTemplate.opsForSet().members(userSetKey);

    if (jtis != null && !jtis.isEmpty()) {
      redisTemplate.delete(
          jtis.stream()
              .map(jti -> REFRESH_JTI_KEY_PREFIX + jti)
              .toList()
      );
    }

    redisTemplate.delete(userSetKey);
  }

  /**
   * ✅ Refresh 재발급(회전, Rotation) - refreshToken 검증 - Redis에 jti 유효성 확인 - 기존 jti 폐기 - 새 access + 새
   * refresh 발급 - 새 refresh 저장
   */
  public TokenPair rotate(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new BusinessException(INVALID_REFRESH_TOKEN);
    }

    RefreshPayload payload;
    try {
      payload = jwtTokenProvider.parseRefreshToken(refreshToken);
    } catch (ExpiredJwtException e) {
      throw new BusinessException(EXPIRED_REFRESH_TOKEN);
    } catch (Exception e) {
      throw new BusinessException(INVALID_REFRESH_TOKEN);
    }

    // Redis에 없는 jti면: 이미 폐기된 토큰(재사용 시도) or TTL 만료
    if (!isValid(payload.userType(), payload.userId(), payload.jti())) {
      throw new BusinessException(REUSED_REFRESH_TOKEN);
    }

    // rotation: 기존 refresh 폐기
    revoke(payload.userId(), payload.jti());

    // 새 토큰 발급
    String newAccess = jwtTokenProvider.createAccessToken(payload.userType(), payload.userId());
    RefreshIssue newRefresh = jwtTokenProvider.createRefreshToken(
        payload.userType(), payload.userId());

    // 새 refresh 저장
    save(payload.userId(), newRefresh.jti(), newRefresh.expiresAt());

    return new TokenPair(newAccess, newRefresh.token());
  }

  public record TokenPair(String accessToken, String refreshToken) {

  }
}
