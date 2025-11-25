package kr.muroom.muroombackendbach.studio.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import lombok.Builder;

public final class StudioResponse {

  private StudioResponse() {
  }

  @Builder
  public record MapBoundsSearch(
      Long id,
      String name,
      Double latitude,
      Double longitude,
      String address
  ) {

    public static MapBoundsSearch from(Studio studio) {
      return MapBoundsSearch.builder()
          .id(studio.getId())
          .name(studio.getName())
          .latitude(studio.getLocation().getX())
          .longitude(studio.getLocation().getY())
          .address(studio.getAddress())
          .build();
    }
  }

  @Builder
  @Schema(description = "지하철 노선 정보")
  public record LineInfo(String lineName, String lineColor) {

  }

  @Builder
  @Schema(description = "인근 지하철역 정보")
  public record SubwayStationInfo(
      String stationName,
      List<LineInfo> lines
  ) {

  }

  @Builder
  @Schema(description = "지도 스튜디오 목록")
  public record MapList(
      @Schema(description = "스튜디오 ID")
      Long studioId,

      @Schema(description = "스튜디오 이름")
      String studioName,

      @Schema(description = "최소 가격", example = "150000")
      Integer minPrice,

      @Schema(description = "최대 가격", example = "430000")
      Integer maxPrice,

      @Schema(description = "인근 지하철역 정보")
      SubwayStationInfo nearbySubwayStationInfo,

      @Schema(description = "썸네일 이미지 Presigned URL")
      String thumbnailImageUrl,

      @Schema(description = "인근 지하철역까지 도보 소요 시간 (분)", example = "8")
      Integer walkingTimeMinutes
  ) {

  }
}
