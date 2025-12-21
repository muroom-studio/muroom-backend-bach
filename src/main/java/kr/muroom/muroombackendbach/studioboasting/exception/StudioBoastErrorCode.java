package kr.muroom.muroombackendbach.studioboasting.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudioBoastErrorCode implements ErrorCode {
  STUDIO_BOAST_NOT_FOUND(HttpStatus.NOT_FOUND, "SB-404-01", "해당 자랑하기 게시글을 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;

}
