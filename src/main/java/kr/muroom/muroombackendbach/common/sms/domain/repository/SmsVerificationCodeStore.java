package kr.muroom.muroombackendbach.common.sms.domain.repository;

import java.time.Duration;

public interface SmsVerificationCodeStore {

  void saveCode(String phoneNumber, String code, Duration ttl);

  String getCode(String phoneNumber);

  void deleteCode(String phoneNumber);

  Long getLastSendEpochMillis(String phoneNumber);

  void updateLastSendEpochMillis(String phoneNumber, long epochMillis);

  int getTodaySendCount(String phoneNumber);

  void resetFailCount(String phoneNumber);

  void incrementTodaySendCount(String phoneNumber);

  int incrementFailCount(String phoneNumber);
}
