package kr.muroom.muroombackendbach.common.sms;

import java.security.SecureRandom;
import java.time.Duration;
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
    String normalizedPhone = PhoneNumberUtil.removeHyphens(phone);
    codeStore.saveCode(normalizedPhone, code, CODE_TTL);

    String content = """
        [Muroom]
        인증번호 [%s]를 입력해 주세요.
        타인에게 절대 알려주지 마세요.
        """.formatted(code);

    smsSender.sendSms(normalizedPhone, content);
  }

  public SmsVerifyResponse verifyCode(String phone, String code) {
    String normalizedPhone = PhoneNumberUtil.removeHyphens(phone);

    String saved = codeStore.getCode(normalizedPhone);

    boolean success = saved != null && saved.equals(code);

    if (success) {
      codeStore.deleteCode(normalizedPhone);
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


