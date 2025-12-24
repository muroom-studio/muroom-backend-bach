package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "스튜디오 지도 응답 DTO")
@Builder
public record StudioMapResponse(
    @Schema(description = "스튜디오 ID", example = "791543436721219205")
    String id,

    @Schema(description = "스튜디오 이름", example = "뮤룸 스튜디오 홍대점")
    String name,

    @Schema(description = "스튜디오 위도", example = "37.5563")
    Double latitude,

    @Schema(description = "스튜디오 경도", example = "126.9227")
    Double longitude,

    @Schema(description = "스튜디오 최소 가격", example = "150000", nullable = true)
    Integer minPrice,

    @Schema(description = "스튜디오 최대 가격", example = "430000", nullable = true)
    Integer maxPrice
) {

}
