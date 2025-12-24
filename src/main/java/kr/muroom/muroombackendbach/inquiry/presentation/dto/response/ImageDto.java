package kr.muroom.muroombackendbach.inquiry.presentation.dto.response;

import lombok.Builder;

@Builder
public record ImageDto(
    String id,
    String imageFileUrl
) {

}