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
      "OA-400-01",
      "지원하지 않는 OAuth 제공자입니다."
  ),
  PROVIDER_USER_TOKEN_NOT_FOUND(
      HttpStatus.BAD_REQUEST,
      "OA-400-02",
      "ID Token 이 존재하지 않습니다."
  ),
  PROVIDER_NOT_RESPONSE(
      HttpStatus.BAD_REQUEST,
      "OA-400-03",
      "certs 응답이 비정상입니다."
  ),
  PROVIDER_INVALID_RESPONSE(
      HttpStatus.BAD_REQUEST,
      "OA-400-04",
      "certs에서 kid에 해당하는 키를 찾지 못했습니다."
  ),
  FAIL_MAKE_PUBLIC_KEY(
      HttpStatus.BAD_REQUEST,
      "OA-400-05",
      "공개키를 만드는데 실패했습니다."
  );

  private final HttpStatus status;
  private final String code;
  private final String message;
}
