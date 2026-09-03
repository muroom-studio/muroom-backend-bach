package kr.muroom.muroombackendbach.report.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportReplyErrorCode implements ErrorCode {
  REPORT_REPLY_NOT_FOUND(HttpStatus.BAD_REQUEST, "RP-400-01", "해당 신고 답변을 찾을 수 없습니다."),
  ;
  private final HttpStatus status;
  private final String code;
  private final String message;

}