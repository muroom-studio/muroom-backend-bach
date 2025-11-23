package kr.muroom.muroombackendbach.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 애플리케이션에서 사용되는 에러 코드를 정의하는 인터페이스입니다.
 *
 * <p>각 에러 코드는 HTTP 상태 코드, 고유한 에러 코드 문자열, 그리고 사용자에게 표시될 메시지를 포함합니다.
 */
public interface ErrorCode {

  /**
   * 에러에 해당하는 HTTP 상태 코드를 반환합니다.
   *
   * @return HTTP 상태 코드
   */
  HttpStatus getStatus();

  /**
   * 에러의 고유한 코드 문자열을 반환합니다.
   *
   * @return 에러 코드 문자열
   */
  String getCode();

  /**
   * 에러 메시지를 반환합니다.
   *
   * @return 에러 메시지
   */
  String getMessage();
}
