package kr.muroom.muroombackendbach.studio.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "스튜디오 주소 검색 요청 DTO")
@Builder
public record StudioSearchByAddressRequest(
    @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 123")
    String roadNameAddress,

    @Schema(description = "지번 주소", example = "서울특별시 강남구 역삼동 456-7")
    String lotNumberAddress
) {

}
