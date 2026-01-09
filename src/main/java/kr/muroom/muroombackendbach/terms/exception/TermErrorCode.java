package kr.muroom.muroombackendbach.terms.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TermErrorCode implements ErrorCode {
  NOT_EXIST_TERM(HttpStatus.NOT_FOUND, "TR-404-01", "존재하지 않는 약관입니다."),
  REQUIRED_TERM_NOT_AGREED(HttpStatus.BAD_REQUEST, "TR-400-01", "필수 약관이 동의되지 않았습니다."),
  TERMS_NOT_REGISTER(HttpStatus.INTERNAL_SERVER_ERROR, "TR-500-01", "약관이 등록되지 않았습니다. 관리자 확인");
  private final HttpStatus status;
  private final String code;
  private final String message;

}
