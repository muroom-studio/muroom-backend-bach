package kr.muroom.muroombackendbach.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직에서 발생하는 예외를 나타내는 클래스입니다.
 *
 * <p>각 예외는 관련된 에러 코드를 포함합니다.
 */
@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  /**
   * 주어진 에러 코드를 사용하여 BusinessException을 생성합니다.
   *
   * @param errorCode 관련된 에러 코드
   */
  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  /**
   * 주어진 에러 코드와 메시지를 사용하여 BusinessException을 생성합니다.
   *
   * @param errorCode 관련된 에러 코드
   * @param message   예외 메시지
   */
  public BusinessException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
