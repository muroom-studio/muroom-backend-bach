package kr.muroom.muroombackendbach.studio.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudioErrorCode implements ErrorCode {
  STUDIO_NOT_FOUND(HttpStatus.BAD_REQUEST, "ST-400-01", "해당 스튜디오를 찾을 수 없습니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;

}
