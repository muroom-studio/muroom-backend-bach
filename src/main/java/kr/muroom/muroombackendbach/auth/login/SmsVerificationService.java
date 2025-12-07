package kr.muroom.muroombackendbach.auth.login;

import java.security.SecureRandom;
import java.time.Duration;
import kr.muroom.muroombackendbach.common.sms.SmsSender;
import kr.muroom.muroombackendbach.common.sms.SmsVerificationCodeStore;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto.VerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsVerificationService {

  private final SmsSender smsSender;
  private final SmsVerificationCodeStore codeStore;
  private static final int CODE_LENGTH = 6;
  private static final Duration CODE_TTL = Duration.ofMinutes(3);

  public void sendVerificationCode(String phone) {
    String code = generateCode();

    codeStore.saveCode(phone, code, CODE_TTL);

    String content = """
        [MUROOM] 인증번호: %s
        타인 유출로 인한 피해 주의
        """.formatted(code);
    smsSender.sendSms(phone, content);
  }

  public VerifyResponse verifyCode(String phone, String code) {
    String saved = codeStore.getCode(phone);

    boolean success = saved != null && saved.equals(code);

    if (success) {
      codeStore.deleteCode(phone);
    }

    return new VerifyResponse(success);
  }

  /**
   * 🔐 0~9 숫자로 이루어진 6자리 랜덤 코드 생성
   */
  private String generateCode() {
    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < CODE_LENGTH; i++) {
      int digit = random.nextInt(10); // 0 ~ 9
      sb.append(digit);
    }
    return sb.toString();
  }

}


