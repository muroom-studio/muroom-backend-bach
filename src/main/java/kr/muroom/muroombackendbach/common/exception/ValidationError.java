package kr.muroom.muroombackendbach.common.exception;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * 유효성 검증 오류 세부 정보를 나타내는 클래스입니다.
 *
 * <p>각 인스턴스는 필드 이름과 해당 필드에서 발생한 문제를 포함합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ValidationError {

  private String field;
  private String issue;

  private ValidationError(final String field, final String issue) {
    this.field = field;
    this.issue = issue;
  }

  /**
   * 주어진 BindingResult에서 유효성 검증 오류 목록을 생성합니다.
   *
   * @param bindingResult 유효성 검증 결과를 포함하는 BindingResult 객체
   * @return ValidationError 객체의 리스트
   */
  public static List<ValidationError> of(final BindingResult bindingResult) {
    final List<FieldError> fieldErrors = bindingResult.getFieldErrors();
    return fieldErrors.stream()
        .map(error -> new ValidationError(
            error.getField(),
            error.getDefaultMessage()))
        .toList();
  }
}
