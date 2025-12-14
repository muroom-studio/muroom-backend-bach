package kr.muroom.muroombackendbach.studio.presentation.dto.response;

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
      String stationName,
      List<StudioSubwayLineInfo> lines,
      Integer distanceMeters
  ) {

  }
}
