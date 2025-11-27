package kr.muroom.muroombackendbach.studio.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public final class StudioRequest {

  private StudioRequest() {
  }

  @Builder
  public record MapBoundsSearch(
      @Schema(description = "최소 위도", example = "37.4")
      Double minLatitude,

      @Schema(description = "최대 위도", example = "37.6")
      Double maxLatitude,

      @Schema(description = "최소 경도", example = "126.9")
      Double minLongitude,

      @Schema(description = "최대 경도", example = "127.2")
      Double maxLongitude
  ) {

  }
}
