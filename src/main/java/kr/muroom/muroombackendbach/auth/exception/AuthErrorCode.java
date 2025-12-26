package kr.muroom.muroombackendbach.auth.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
  FORBIDDEN(HttpStatus.FORBIDDEN, "AU-403-01", "해당 리소스에 접근 권한이 없습니다."),
  LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "AU-401-02", "로그인 실패"),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
