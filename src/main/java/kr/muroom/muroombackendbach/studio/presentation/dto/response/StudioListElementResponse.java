package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayStationInfo;
import lombok.Builder;

@Builder
@Schema(description = "지도 스튜디오 목록")
public record StudioListElementResponse(
    @Schema(description = "스튜디오 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "791543436721219205")
    String studioId,

    @Schema(description = "스튜디오 이름", example = "뮤룸 스튜디오 홍대점", requiredMode = Schema.RequiredMode.REQUIRED)
    String studioName,

    @Schema(description = "최소 가격", example = "150000", nullable = true)
    Integer minPrice,

    @Schema(description = "최대 가격", example = "430000", nullable = true)
    Integer maxPrice,

    @Schema(description = "인근 지하철역 정보", requiredMode = Schema.RequiredMode.REQUIRED)
    StudioSubwayStationInfo nearbySubwayStationInfo,

    @Schema(description = "썸네일 이미지 Presigned URL", requiredMode = Schema.RequiredMode.REQUIRED)
    String thumbnailImageUrl,

    @Schema(description = "경도", example = "126.9780", requiredMode = Schema.RequiredMode.REQUIRED)
    Double longitude,

    @Schema(description = "위도", example = "37.5665", requiredMode = Schema.RequiredMode.REQUIRED)
    Double latitude
) {

}
