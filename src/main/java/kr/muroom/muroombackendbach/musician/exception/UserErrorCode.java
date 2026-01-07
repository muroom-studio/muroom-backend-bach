package kr.muroom.muroombackendbach.musician.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
  NICKNAME_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "US-409-01", "이미 존재하는 닉네임 입니다."),
  ALREADY_EXIST_EMAIL(HttpStatus.BAD_REQUEST, "US-409-02", "이미 존재하는 이메일 입니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "US-404-01", "사용자를 찾을 수 없습니다."),
  PHONENUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "US-409-03", "이미 존재하는 전화번호 입니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;

}
