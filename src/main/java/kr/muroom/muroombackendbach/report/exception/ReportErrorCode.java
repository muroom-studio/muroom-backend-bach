package kr.muroom.muroombackendbach.report.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {
  REPORT_NOT_FOUND(HttpStatus.BAD_REQUEST, "RP-400-01", "해당 신고내역을 찾을 수 없습니다."),
  REPORT_REASON_NOT_FOUND(HttpStatus.BAD_REQUEST, "RP-400-02", "해당 신고이유 유형을 찾을 수 없습니다."),
  REPORT_UNSUPPORTED_TARGET_TYPE(HttpStatus.BAD_REQUEST, "RP-400-03", "해당 도메인을 신고할 수 없습니다."),
  REPORT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "RP-400-04", "이미 신고 처리 되었습니다."),
  REPORT_SNAPSHOT_SERIALIZE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RP-500-05",
      "JSON 변환 실패했습니다."),
  REPORT_FORBIDDEN(HttpStatus.FORBIDDEN, "RP-403-06", "권한이 없습니다."),
  REPORT_NOT_EDITABLE(HttpStatus.BAD_REQUEST, "RP-400-07", "수정을 할 수 없는 상태의 신고입니다."),
  REPORT_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "RP-400-08", "잘못된 요청 값 입니다."),
  REPORT_NOT_ME(HttpStatus.BAD_REQUEST, "RP-400-09", "나를 신고할 수 없습니다."),
  ;
  private final HttpStatus status;
  private final String code;
  private final String message;

}


