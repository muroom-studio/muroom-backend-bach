package kr.muroom.muroombackendbach.common.presentation.response;

import lombok.Builder;

@Builder
public record FieldErrorDetail(
    String field,
    String rejectedValue,
    String reason
) {

}
