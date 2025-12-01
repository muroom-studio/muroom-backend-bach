package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import lombok.Builder;

@Builder
public record StudioMapResponse(
    Long id,
    String name,
    Double latitude,
    Double longitude,
    Integer minPrice,
    Integer maxPrice
) {

}
