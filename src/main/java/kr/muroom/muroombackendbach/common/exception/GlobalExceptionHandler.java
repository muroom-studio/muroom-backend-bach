package kr.muroom.muroombackendbach.common.exception;

import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.auth.auth.exception.AuthErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전반에서 발생하는 예외를 처리하는 글로벌 예외 처리기 클래스입니다.
 *
 * <p>각 예외에 대해 적절한 HTTP 응답을 생성하여 클라이언트에게 반환합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 유효성 검증 실패 예외를 처리합니다.
   *
   * @param e MethodArgumentNotValidException 유효성 검증 실패 예외
   * @return {@code ResponseEntity<ApiResponse<ErrorPayload>>} 응답 엔티티
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ApiResponse<ErrorPayload>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    log.warn("[# MethodArgumentNotValidException] ", e);
    final ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
    final List<ValidationError> validationErrors = ValidationError.of(e.getBindingResult());
    final ApiResponse<ErrorPayload> response = ApiResponse.fail(errorCode.getStatus(),
        errorCode.getCode(), errorCode.getMessage(), validationErrors);
    return new ResponseEntity<>(response, errorCode.getStatus());
  }

  /**
   * 바인딩 실패 예외를 처리합니다.
   *
   * @param e BindException 바인딩 실패 예외
   * @return {@code ResponseEntity<ApiResponse<ErrorPayload>>} 응답 엔티티
   */
  @ExceptionHandler(BindException.class)
  protected ResponseEntity<ApiResponse<ErrorPayload>> handleBindException(BindException e) {
    log.warn("[# BindException] ", e);
    final ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
    final List<ValidationError> validationErrors = ValidationError.of(e.getBindingResult());
    final ApiResponse<ErrorPayload> response = ApiResponse.fail(errorCode.getStatus(),
        errorCode.getCode(), errorCode.getMessage(), validationErrors);
    return new ResponseEntity<>(response, errorCode.getStatus());
  }

  /**
   * 비즈니스 예외를 처리합니다.
   *
   * @param e BusinessException 비즈니스 예외
   * @return {@code ResponseEntity<ApiResponse<ErrorPayload>>} 응답 엔티티
   */
  @ExceptionHandler(BusinessException.class)
  protected ResponseEntity<ApiResponse<ErrorPayload>> handleBusinessException(
      final BusinessException e) {
    log.warn("[# BusinessException] ", e);
    final ErrorCode errorCode = e.getErrorCode();
    final ApiResponse<ErrorPayload> response = ApiResponse.fail(errorCode.getStatus(),
        errorCode.getCode(), e.getMessage());
    return new ResponseEntity<>(response, errorCode.getStatus());
  }

  /**
   * 외부 API 연동 예외를 처리합니다.
   *
   * @param e ExternalApiException 외부 API 연동 예외
   * @return {@code ResponseEntity<ApiResponse<ErrorPayload>>} 응답 엔티티
   */
  @ExceptionHandler(ExternalApiException.class)
  protected ResponseEntity<ApiResponse<ErrorPayload>> handleExternalApiException(
      final ExternalApiException e) {
    // TODO: 슬랙/메일/모니터링 시스템 연동
    log.error("[# ExternalApiException] ", e);

    final ErrorCode errorCode = CommonErrorCode.EXTERNAL_SERVICE_UNAVAILABLE;

    final ApiResponse<ErrorPayload> response = ApiResponse.fail(
        errorCode.getStatus(),
        errorCode.getCode(),
        "외부 시스템 연동 중 오류가 발생했습니다."
    );

    return new ResponseEntity<>(response, errorCode.getStatus());
  }

  /**
   * 지원되지 않는 HTTP 메서드 예외를 처리합니다.
   *
   * @param e HttpRequestMethodNotSupportedException 지원되지 않는 HTTP 메서드 예외
   * @return {@code ResponseEntity<ApiResponse<ErrorPayload>>} 응답 엔티티
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  protected ResponseEntity<ApiResponse<ErrorPayload>> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException e) {
    log.warn("[# HttpRequestMethodNotSupportedException] ", e);
    final ErrorCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;
    final ApiResponse<ErrorPayload> response = ApiResponse.fail(errorCode.getStatus(),
        errorCode.getCode(), errorCode.getMessage());
    return new ResponseEntity<>(response, errorCode.getStatus());
  }

  /**
   * 그 외 일반 예외를 처리합니다.
   *
   * @param e Exception 일반 예외
   * @return {@code ResponseEntity<ApiResponse<ErrorPayload>>} 응답 엔티티
   */
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ApiResponse<ErrorPayload>> handleUnexpectedException(Exception e) {
    log.error("[# UnexpectedException] ", e);
    final ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
    final ApiResponse<ErrorPayload> response = ApiResponse.fail(errorCode.getStatus(),
        errorCode.getCode(), errorCode.getMessage());
    return new ResponseEntity<>(response, errorCode.getStatus());
  }

  /**
   * 로그인 예외 핸들러
   *
   */
  @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
  public ResponseEntity<ApiResponse<ErrorPayload>> handle(Exception ex) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    boolean notLoggedIn =
        auth == null ||
            auth instanceof AnonymousAuthenticationToken ||
            !auth.isAuthenticated();

    if (notLoggedIn) {
      final ErrorCode errorCode = AuthErrorCode.UNAUTHORIZED;
      final ApiResponse<ErrorPayload> response = ApiResponse.fail(errorCode.getStatus(),
          errorCode.getCode(), errorCode.getMessage());
      return new ResponseEntity<>(response, errorCode.getStatus()); // 401
    }

    final ErrorCode errorCode = AuthErrorCode.FORBIDDEN;
    final ApiResponse<ErrorPayload> response = ApiResponse.fail(errorCode.getStatus(),
        errorCode.getCode(), errorCode.getMessage());
    return new ResponseEntity<>(response, errorCode.getStatus()); // 403
  }
}
