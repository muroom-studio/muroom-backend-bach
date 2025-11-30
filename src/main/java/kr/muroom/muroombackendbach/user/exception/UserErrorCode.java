package kr.muroom.muroombackendbach.user.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
  ALREADY_EXIST_NICKNAME(HttpStatus.BAD_REQUEST, "US-400-01", "이미 존재하는 닉네임 입니다."),
  ALREADY_EXIST_EMAIL(HttpStatus.BAD_REQUEST, "US-400-02", "이미 존재하는 이메일 입니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;

}
