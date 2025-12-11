package kr.muroom.muroombackendbach.common.sms.domain.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisSmsVerificationCodeStore implements SmsVerificationCodeStore {

  private static final String PREFIX = "sms:verify:";
  private final StringRedisTemplate redisTemplate;

  @Override
  public void saveCode(String phoneNumber, String code, Duration ttl) {
    String key = PREFIX + phoneNumber;
    redisTemplate.opsForValue().set(key, code, ttl);
  }

  @Override
  public String getCode(String phoneNumber) {
    String key = PREFIX + phoneNumber;
    return redisTemplate.opsForValue().get(key);
  }

  @Override
  public void deleteCode(String phoneNumber) {
    String key = PREFIX + phoneNumber;
    redisTemplate.delete(key);
  }
}
