package kr.muroom.muroombackendbach.auth.jwt.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JwtErrorCode implements ErrorCode {

  INVALID_SIGNUP_TOKEN(
      HttpStatus.BAD_REQUEST,
      "JWT-400-01",
      "유효하지 않은 회원가입 토큰입니다."
  ),

  MISSING_SIGNUP_TOKEN_CLAIMS(
      HttpStatus.BAD_REQUEST,
      "JWT-400-02",
      "회원가입 토큰에 필수 정보가 없습니다."
  );

  private final HttpStatus status;
  private final String code;
  private final String message;
}
