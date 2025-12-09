package kr.muroom.muroombackendbach.common.sms;

import static kr.muroom.muroombackendbach.common.sms.SmsErrorCode.INVALID_PHONE_NUMBER;

import java.security.SecureRandom;
import java.time.Duration;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.util.PhoneNumberUtil;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto.SmsVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsVerificationService {

  private final SmsSender smsSender;
  private final SmsVerificationCodeStore codeStore;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final int CODE_LENGTH = 6;
  private static final Duration CODE_TTL = Duration.ofMinutes(3);

  public void sendVerificationCode(String phone) {
    String code = generateCode();
    
    String normalizedPhone = PhoneNumberUtil.normalize(phone);
    codeStore.saveCode(normalizedPhone, code, CODE_TTL);

    String content = """
        [MUROOM] 인증번호: %s
        타인 유출로 인한 피해 주의
        """.formatted(code);
    smsSender.sendSms(normalizedPhone, content);
  }

  public SmsVerifyResponse verifyCode(String phone, String code) {
    String saved = codeStore.getCode(phone);

    boolean success = saved != null && saved.equals(code);

    if (success) {
      codeStore.deleteCode(phone);
    }

    return new SmsVerifyResponse(success);
  }

  /**
   * 🔐 0~9 숫자로 이루어진 6자리 랜덤 코드 생성
   */
  private String generateCode() {
    StringBuilder sb = new StringBuilder(CODE_LENGTH);
    for (int i = 0; i < CODE_LENGTH; i++) {
      sb.append(SECURE_RANDOM.nextInt(10)); // 0~9
    }
    return sb.toString();
  }

}


