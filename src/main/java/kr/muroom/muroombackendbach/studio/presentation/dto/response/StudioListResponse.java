package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayStationInfo;
import lombok.Builder;

@Builder
@Schema(description = "지도 스튜디오 목록")
public record StudioListResponse(
    @Schema(description = "스튜디오 ID")
    Long studioId,

    @Schema(description = "스튜디오 이름")
    String studioName,

    @Schema(description = "최소 가격", example = "150000")
    Integer minPrice,

    @Schema(description = "최대 가격", example = "430000")
    Integer maxPrice,

    @Schema(description = "인근 지하철역 정보")
    StudioSubwayStationInfo nearbySubwayStationInfo,

    @Schema(description = "썸네일 이미지 Presigned URL")
    String thumbnailImageUrl,

    @Schema(description = "인근 지하철역까지 도보 소요 시간 (분)", example = "8")
    Integer walkingTimeMinutes
) {

}
