package kr.muroom.muroombackendbach.studio.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

public final class StudioResponse {

  private StudioResponse() {
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
}
