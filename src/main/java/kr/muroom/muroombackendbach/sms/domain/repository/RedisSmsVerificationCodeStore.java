package kr.muroom.muroombackendbach.sms.domain.repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisSmsVerificationCodeStore implements SmsVerificationCodeStore {

  private static final String PREFIX_CODE = "sms:verify:";
  private static final String PREFIX_LAST_SEND = "sms:last_send:";
  private static final String PREFIX_DAILY_COUNT = "sms:daily_count:";
  private static final String PREFIX_FAIL_COUNT = "sms:fail_count:";
  private static final String PREFIX_DAILY_COUNT_IP = "sms:ip:daily_count:";

  private final StringRedisTemplate redisTemplate;

  @Override
  public void saveCode(String phoneNumber, String code, Duration ttl) {
    String key = PREFIX_CODE + phoneNumber;
    redisTemplate.opsForValue().set(key, code, ttl);
  }

  @Override
  public String getCode(String phoneNumber) {
    return redisTemplate.opsForValue().get(PREFIX_CODE + phoneNumber);
  }

  @Override
  public void deleteCode(String phoneNumber) {
    redisTemplate.delete(PREFIX_CODE + phoneNumber);
  }

  @Override
  public Long getLastSendEpochMillis(String phoneNumber) {
    String value = redisTemplate.opsForValue().get(PREFIX_LAST_SEND + phoneNumber);
    return value != null ? Long.parseLong(value) : null;
  }

  @Override
  public void updateLastSendEpochMillis(String phoneNumber, long epochMillis) {
    String key = PREFIX_LAST_SEND + phoneNumber;
    redisTemplate.opsForValue().set(key, String.valueOf(epochMillis), Duration.ofMinutes(1));
  }

  @Override
  public int getTodaySendCount(String phoneNumber) {
    String key = dailyCountKey(phoneNumber);
    String value = redisTemplate.opsForValue().get(key);
    return value != null ? Integer.parseInt(value) : 0;
  }

  @Override
  public void resetFailCount(String phoneNumber) {
    redisTemplate.delete(PREFIX_FAIL_COUNT + phoneNumber);
  }

  @Override
  public void incrementTodaySendCount(String phoneNumber) {
    String key = dailyCountKey(phoneNumber);
    redisTemplate.opsForValue().increment(key);

    redisTemplate.expire(key, durationUntilMidnight());
  }

  @Override
  public int incrementFailCount(String phoneNumber) {
    String key = PREFIX_FAIL_COUNT + phoneNumber;
    Long value = redisTemplate.opsForValue().increment(key);
    return value != null ? value.intValue() : 0;
  }

  @Override
  public int getTodaySendCountByIp(String ip) {
    String key = dailyCountKeyByIp(ip);
    String value = redisTemplate.opsForValue().get(key);
    return value != null ? Integer.parseInt(value) : 0;
  }

  @Override
  public int incrementTodaySendCountByIp(String ip) {
    String key = dailyCountKeyByIp(ip);
    Long value = redisTemplate.opsForValue().increment(key);

    redisTemplate.expire(key, durationUntilMidnight());

    return value != null ? value.intValue() : 0;
  }

  private String dailyCountKeyByIp(String ip) {
    String today = LocalDate.now().toString();
    return PREFIX_DAILY_COUNT_IP + ip + ":" + today;
  }

  private String dailyCountKey(String phoneNumber) {
    String today = LocalDate.now().toString();
    return PREFIX_DAILY_COUNT + phoneNumber + ":" + today;
  }

  private Duration durationUntilMidnight() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);

    long seconds = midnight.atZone(ZoneId.systemDefault()).toEpochSecond()
        - now.atZone(ZoneId.systemDefault()).toEpochSecond();

    return Duration.ofSeconds(seconds);
  }
}
