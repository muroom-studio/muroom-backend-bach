package kr.muroom.muroombackendbach.auth.oauth.login.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OAuthLoginErrorCode implements ErrorCode {

  UNSUPPORTED_OAUTH_PROVIDER(
      HttpStatus.BAD_REQUEST,
      "OAUTH-400-01",
      "지원하지 않는 OAuth 제공자입니다."
  );

  private final HttpStatus status;
  private final String code;
  private final String message;
}
