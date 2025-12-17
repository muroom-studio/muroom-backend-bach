package kr.muroom.muroombackendbach.inquiry.presentation.dto;

import lombok.Builder;

@Builder
public record InquiryCategoryResponse(
    Long id,
    String code,
    String name
) {

}
