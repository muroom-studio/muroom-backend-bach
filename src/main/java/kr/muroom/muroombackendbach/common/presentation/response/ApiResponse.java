package kr.muroom.muroombackendbach.common.presentation.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
    int status,
    String message,
    @JsonInclude(JsonInclude.Include.NON_NULL) T data
) {

  public static ApiResponse<Void> success() {
    return success(null);
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(HttpStatus.OK.value(), "요청에 성공했습니다.", data);
  }

  public static ApiResponse<Void> created() {
    return created(null);
  }

  public static <T> ApiResponse<T> created(T data) {
    return new ApiResponse<>(HttpStatus.CREATED.value(), "성공적으로 생성되었습니다.", data);
  }

  public static ApiResponse<Void> deleted() {
    return new ApiResponse<>(HttpStatus.NO_CONTENT.value(), "성공적으로 삭제되었습니다.", null);
  }
}