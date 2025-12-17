package kr.muroom.muroombackendbach.inquiry.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InquiryErrorCode implements ErrorCode {
  INQUIRY_NOT_FOUND(HttpStatus.BAD_REQUEST, "IQ-400-01", "존재하지 않는 문의입니다."),
  INQUIRY_FORBIDDEN(HttpStatus.BAD_REQUEST, "IQ-400-02", "권한이 없습니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}