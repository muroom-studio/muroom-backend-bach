package kr.muroom.muroombackendbach.beta.registration.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RegistrationErrorCode implements ErrorCode {

  UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "BT-400-01", "지원하지 않는 파일 형식입니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
