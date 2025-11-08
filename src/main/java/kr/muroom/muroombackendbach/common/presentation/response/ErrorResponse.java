package kr.muroom.muroombackendbach.common.presentation.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String code,
    String message,
    List<FieldErrorDetail> errorDetails
) {

  public static ErrorResponse of(String code, String message) {
    return new ErrorResponse(code, message, null);
  }

  public static ErrorResponse of(String code, String message, List<FieldErrorDetail> errorDetails) {
    return new ErrorResponse(code, message, errorDetails);
  }

  public static ErrorResponse of(String code, String message, FieldErrorDetail errorDetail) {
    return new ErrorResponse(code, message, List.of(errorDetail));
  }
}
