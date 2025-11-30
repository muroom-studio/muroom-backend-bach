package kr.muroom.muroombackendbach.terms.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TermErrorCode implements ErrorCode {
  NOT_EXIST_TERM(HttpStatus.BAD_REQUEST, "TR-400-01", "존재하지 않는 약관입니다.");
  private final HttpStatus status;
  private final String code;
  private final String message;

}
