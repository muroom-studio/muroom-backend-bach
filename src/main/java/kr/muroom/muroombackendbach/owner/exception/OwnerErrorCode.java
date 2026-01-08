package kr.muroom.muroombackendbach.owner.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OwnerErrorCode implements ErrorCode {
  NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "OW-409-01", "이미 존재하는 닉네임 입니다."),
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "OW-409-02", "이미 존재하는 이메일 입니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "OW-404-01", "사용자를 찾을 수 없습니다."),
  PHONENUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "OW-409-03", "이미 존재하는 전화번호 입니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
