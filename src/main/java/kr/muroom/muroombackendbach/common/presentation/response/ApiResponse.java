package kr.muroom.muroombackendbach.common.presentation.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.muroom.muroombackendbach.common.exception.ErrorPayload;
import kr.muroom.muroombackendbach.common.exception.ValidationError;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * API 응답을 나타내는 레코드 클래스입니다.
 *
 * <p>응답 상태, 메시지 및 선택적으로 데이터 페이로드를 포함합니다.
 *
 * @param <T> 응답 데이터의 타입
 */
public record ApiResponse<T>(
    int status,
    String message,
    @JsonInclude(JsonInclude.Include.NON_NULL) T data
) {

  /**
   * 성공적인 응답을 생성합니다.
   *
   * @return 성공적인 ApiResponse 인스턴스
   */
  public static ApiResponse<Void> success() {
    return success(null);
  }

  /**
   * 성공적인 응답을 생성합니다.
   *
   * @param data 응답 데이터
   * @param <T>  응답 데이터의 타입
   * @return 성공적인 ApiResponse 인스턴스
   */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(HttpStatus.OK.value(), "요청에 성공했습니다.", data);
  }

  /**
   * 생성된 응답을 생성합니다.
   *
   * @return 생성된 ApiResponse 인스턴스
   */
  public static ApiResponse<Void> created() {
    return created(null);
  }

  /**
   * 생성된 응답을 생성합니다.
   *
   * @param data 응답 데이터
   * @param <T>  응답 데이터의 타입
   * @return 생성된 ApiResponse 인스턴스
   */
  public static <T> ApiResponse<T> created(T data) {
    return new ApiResponse<>(HttpStatus.CREATED.value(), "성공적으로 생성되었습니다.", data);
  }

  /**
   * 삭제된 응답을 생성합니다.
   *
   * @return 삭제된 ApiResponse 인스턴스
   */
  public static ApiResponse<Void> deleted() {
    return new ApiResponse<>(HttpStatus.OK.value(), "성공적으로 삭제되었습니다.", null);
  }

  /**
   * 실패한 응답을 생성합니다.
   *
   * @param status  HTTP 상태 코드
   * @param code    에러 코드
   * @param message 에러 메시지
   * @return 실패한 ApiResponse 인스턴스
   */
  public static ApiResponse<ErrorPayload> fail(HttpStatus status, String code, String message) {
    return new ApiResponse<>(status.value(), message, new ErrorPayload(code, null));
  }

  /**
   * 실패한 응답을 생성합니다.
   *
   * @param status  HTTP 상태 코드
   * @param code    에러 코드
   * @param message 에러 메시지
   * @param details 유효성 검증 오류 상세 정보 목록
   * @return 실패한 ApiResponse 인스턴스
   */
  public static ApiResponse<ErrorPayload> fail(HttpStatus status, String code, String message,
      List<ValidationError> details) {
    return new ApiResponse<>(status.value(), message, new ErrorPayload(code, details));
  }
}