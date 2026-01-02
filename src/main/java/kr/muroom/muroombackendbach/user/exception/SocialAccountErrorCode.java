package kr.muroom.muroombackendbach.user.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SocialAccountErrorCode implements ErrorCode {
  SOCIAL_ACCOUNT_NOT_FOUND(HttpStatus.BAD_REQUEST, "SA-404-01", "존재하지 않는 소셜 계정입니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;

}
