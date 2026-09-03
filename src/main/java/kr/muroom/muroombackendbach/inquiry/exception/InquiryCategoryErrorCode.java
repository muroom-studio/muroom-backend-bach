package kr.muroom.muroombackendbach.inquiry.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InquiryCategoryErrorCode implements ErrorCode {
  INQUIRY_CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "IQ-400-01", "존재하지 문의 카테고리 입니다.");
  private final HttpStatus status;
  private final String code;
  private final String message;
}
