package kr.muroom.muroombackendbach.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전반에서 시스템 공통으로 사용되는 에러 코드를 정의하는 Enum 클래스입니다.
 *
 * <p>{@link ErrorCode} 인터페이스를 구현하며, 각 에러 코드는 HTTP 상태 코드, 고유한 에러 코드 문자열,
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

  INVALID_INPUT(HttpStatus.BAD_REQUEST, "CM-400-01", "입력값이 올바르지 않습니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "CM-405-01", "허용되지 않은 요청입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CM-500-01", "서버 내부 오류가 발생했습니다."),

  EXTERNAL_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "CM-503-01",
      "외부 서비스 이용이 일시적으로 불가능합니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
