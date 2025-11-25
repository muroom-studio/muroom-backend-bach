package kr.muroom.muroombackendbach.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 에러 응답의 페이로드를 나타내는 레코드 클래스입니다.
 *
 * <p>에러 코드와 선택적으로 유효성 검증 오류 {@link ValidationError} 목록을 포함합니다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorPayload(
    String code,
    @JsonInclude(JsonInclude.Include.NON_NULL) List<ValidationError> details
) {

}
