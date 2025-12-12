package kr.muroom.muroombackendbach.common.sms.application;

import static kr.muroom.muroombackendbach.common.sms.exception.SmsErrorCode.SMS_CODE_NOT_REQUESTED;
import static kr.muroom.muroombackendbach.common.sms.exception.SmsErrorCode.SMS_DAILY_LIMIT_EXCEEDED;
import static kr.muroom.muroombackendbach.common.sms.exception.SmsErrorCode.SMS_RESEND_TOO_FAST;
import static kr.muroom.muroombackendbach.common.sms.exception.SmsErrorCode.SMS_VERIFICATION_FAIL_LIMIT_EXCEEDED;

import java.security.SecureRandom;
import java.time.Duration;

import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.sms.domain.repository.SmsVerificationCodeStore;
import kr.muroom.muroombackendbach.common.sms.presentation.SmsSender;
import kr.muroom.muroombackendbach.common.sms.presentation.dto.SmsAuthResponse;
import kr.muroom.muroombackendbach.common.util.PhoneNumberUtil;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto.SmsVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SmsVerificationService {

  private final SmsSender smsSender;
  private final SmsVerificationCodeStore codeStore;

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final int CODE_LENGTH = 6;

  private static final Duration CODE_TTL = Duration.ofMinutes(3);
  private static final Duration RESEND_COOLDOWN = Duration.ofMinutes(1);

  private static final int DAILY_LIMIT = 5;
  private static final int MAX_FAIL_COUNT = 5;

  @Transactional
  public SmsAuthResponse sendVerificationCode(String phone) {
    // 1. 전화번호 - 하이픈 제거
    String normalizedPhone = normalizePhone(phone);

    // 2. 하루 최대 전송 / 재요청 시간 검사
    checkRateLimit(normalizedPhone);

    // 3. 인증 코드 생성
    String code = generateCode();
    codeStore.saveCode(normalizedPhone, code, CODE_TTL);

    String content = """
        [Muroom]
        인증번호 [%s]를 입력해 주세요.
        타인에게 절대 알려주지 마세요.
        """.formatted(code);

    // 4. 문자 전송
    smsSender.sendSms(normalizedPhone, content);

    // 5. 문자 전송 시간 / 하루 전송 제한 카운팅 / 실패 횟수 초기화
    codeStore.updateLastSendEpochMillis(normalizedPhone, System.currentTimeMillis());
    codeStore.incrementTodaySendCount(normalizedPhone);
    codeStore.resetFailCount(normalizedPhone);

    // 6. 오늘 전송 가능 남은 요청 횟수 응답
    int todayCount = codeStore.getTodaySendCount(normalizedPhone);
    int remainingDailySendCount = Math.max(DAILY_LIMIT - todayCount, 0);
    return new SmsAuthResponse(remainingDailySendCount);
  }

  @Transactional
  public SmsVerifyResponse verifyCode(String phone, String code) {
    // 1. 전화번호 - 하이픈 제거
    String normalizedPhone = normalizePhone(phone);

    // 2. 코드 검증
    String saved = codeStore.getCode(normalizedPhone);
    if (saved == null) {
      throw new BusinessException(SMS_CODE_NOT_REQUESTED);
    }

    boolean success = saved.equals(code);

    // 3. 성공
    if (success) {
      // 3.1 저장된 인증 코드 삭제 / 실패 횟수 초기화
      codeStore.deleteCode(normalizedPhone);
      codeStore.resetFailCount(normalizedPhone);
      return new SmsVerifyResponse(true);
    }

    // 4. 실패 실패 횟수 증가
    int failCount = codeStore.incrementFailCount(normalizedPhone);

    // 4.1 최대 인증 횟수 초과 검증
    expireIfExceededFailCount(normalizedPhone, failCount);

    return new SmsVerifyResponse(false);
  }

  private String generateCode() {
    StringBuilder sb = new StringBuilder(CODE_LENGTH);
    for (int i = 0; i < CODE_LENGTH; i++) {
      sb.append(SECURE_RANDOM.nextInt(10));
    }
    return sb.toString();
  }

  private void checkRateLimit(String phone) {
    long now = System.currentTimeMillis();

    Long lastSendAt = codeStore.getLastSendEpochMillis(phone);
    if (lastSendAt != null) {
      long diffMillis = now - lastSendAt;
      if (diffMillis < RESEND_COOLDOWN.toMillis()) {
        throw new BusinessException(SMS_RESEND_TOO_FAST);
      }
    }

    int todayCount = codeStore.getTodaySendCount(phone);
    if (todayCount >= DAILY_LIMIT) {
      throw new BusinessException(SMS_DAILY_LIMIT_EXCEEDED);
    }
  }

  private void expireIfExceededFailCount(String phone, int failCount) {
    if (failCount >= MAX_FAIL_COUNT) {
      codeStore.deleteCode(phone);
      codeStore.resetFailCount(phone);
      throw new BusinessException(SMS_VERIFICATION_FAIL_LIMIT_EXCEEDED);
    }
  }

  private String normalizePhone(String phone) {
    return PhoneNumberUtil.removeHyphens(phone);
  }
}
