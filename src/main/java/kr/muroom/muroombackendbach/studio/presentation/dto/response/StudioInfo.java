package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

public final class StudioInfo {

  private StudioInfo() {
  }

  @Builder
  public record StudioPriceInfo(Integer minPrice, Integer maxPrice) {

  }

  @Builder
  public record StudioSubwayLineInfo(String lineName, String lineColor) {

  }

  @Builder
  public record StudioSubwayStationInfo(
      @Schema(description = "지하철역 이름", example = "홍대입구역", requiredMode = Schema.RequiredMode.REQUIRED)
      String stationName,

      @Schema(description = "지하철 노선들 정보", requiredMode = Schema.RequiredMode.REQUIRED)
      List<StudioSubwayLineInfo> lines,

      @Schema(description = "스튜디오와 지하철역 간의 거리(단위: m)", example = "350", requiredMode = Schema.RequiredMode.REQUIRED)
      Integer distanceInMeters
  ) {

  }
}
