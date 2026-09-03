package kr.muroom.muroombackendbach.inquiry.presentation.dto.response;

import lombok.Builder;

@Builder
public record InquiryCategoryResponse(
    String id,
    String code,
    String name
) {

}
