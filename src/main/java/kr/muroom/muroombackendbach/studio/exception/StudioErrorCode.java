package kr.muroom.muroombackendbach.studio.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudioErrorCode implements ErrorCode {
  NOT_EXIST_STUDIO(HttpStatus.BAD_REQUEST, "ST-400-01", "존재하지않는 작업실입니다."),
  ALREADY_EXIST_EMAIL(HttpStatus.BAD_REQUEST, "ST-400-02", "이미 존재하는 이메일 입니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;

}
