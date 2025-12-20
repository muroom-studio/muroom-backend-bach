package kr.muroom.muroombackendbach.faq.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FaqErrorCode implements ErrorCode {
  ;
  private final HttpStatus status;
  private final String code;
  private final String message;
}
