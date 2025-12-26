package kr.muroom.muroombackendbach.studioboasting.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudioBoastErrorCode implements ErrorCode {
  STUDIO_BOAST_NOT_FOUND(HttpStatus.BAD_REQUEST, "SB-404-01", "해당 작업실 소개(자랑)글을 찾을 수 없습니다."),

  STUDIO_BOAST_COMMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "SB-404-11", "해당 작업실 소개(자랑)글의 댓글을 찾을 수 없습니다."),

  EVENT_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "SB-400-91", "인스타그램 계정 입력 시 이벤트 약관에 동의해야 합니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;

}
