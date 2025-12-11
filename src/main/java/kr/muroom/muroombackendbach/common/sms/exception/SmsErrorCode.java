package kr.muroom.muroombackendbach.common.sms.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SmsErrorCode implements ErrorCode {

  INVALID_PHONE_NUMBER(
      HttpStatus.BAD_REQUEST,
      "SM-400-01",
      "잘못된 핸드폰 번호입니다."
  ),

  SMS_RESEND_TOO_FAST(
      HttpStatus.BAD_REQUEST,
      "SM-400-02",
      "너무 빠르게 요청하셨습니다. 잠시 후 다시 시도해주세요."
  ),

  SMS_DAILY_LIMIT_EXCEEDED(
      HttpStatus.BAD_REQUEST,
      "SM-400-03",
      "오늘은 더 이상 인증번호를 보낼 수 없습니다. 내일 다시 시도해주세요."
  ),

  SMS_VERIFICATION_FAIL_LIMIT_EXCEEDED(
      HttpStatus.BAD_REQUEST,
      "SM-400-04",
      "인증 실패 횟수를 초과했습니다. 새로운 인증번호를 다시 요청해주세요."
  );

  private final HttpStatus status;
  private final String code;
  private final String message;
}
