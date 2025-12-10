package kr.muroom.muroombackendbach.common.sms;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SmsErrorCode implements ErrorCode {
  INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "SM-400-01", "잘못된 핸드폰 번호 입니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;

}
