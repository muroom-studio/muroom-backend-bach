package kr.muroom.muroombackendbach.inquiry.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InquiryReplyErrorCode implements ErrorCode {
  INQUIRY_REPLY_NOT_FOUND(HttpStatus.BAD_REQUEST, "IQ-404-01", "존재하지 않는 문의 답변입니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}