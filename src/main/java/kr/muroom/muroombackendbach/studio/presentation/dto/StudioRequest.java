package kr.muroom.muroombackendbach.studio.presentation.dto;

import lombok.Builder;

public final class StudioRequest {

  private StudioRequest() {
  }

  @Builder
  public record MapBoundsSearch(
      Double minLatitude, Double maxLatitude,
      Double minLongitude, Double maxLongitude
  ) {

  }
}
