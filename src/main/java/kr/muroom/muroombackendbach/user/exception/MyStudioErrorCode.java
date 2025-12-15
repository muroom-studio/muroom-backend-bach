package kr.muroom.muroombackendbach.user.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MyStudioErrorCode implements ErrorCode {
  MY_STUDIO_NOT_FOUND(HttpStatus.BAD_REQUEST, "MS-400-01", "존재하지 않는 나의 작업실입니다.");
  private final HttpStatus status;
  private final String code;
  private final String message;
}
