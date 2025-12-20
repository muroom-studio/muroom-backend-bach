package kr.muroom.muroombackendbach.faq.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FaqErrorCode implements ErrorCode {
  FAQ_CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "FQ-404-01", "존재하지 않는 FAQ 카테고리입니다."),
  FAQ_NOT_FOUND(HttpStatus.BAD_REQUEST, "FQ-404-02", "존재하지 않는 FAQ 입니다.");
  private final HttpStatus status;
  private final String code;
  private final String message;
}
