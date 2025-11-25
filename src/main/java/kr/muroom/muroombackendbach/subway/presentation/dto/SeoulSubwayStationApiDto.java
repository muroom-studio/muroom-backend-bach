package kr.muroom.muroombackendbach.subway.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

public final class SeoulSubwayStationApiDto {

  private SeoulSubwayStationApiDto() {
  }

  public record Response(
      @JsonProperty("subwayStationMaster")
      Result result
  ) {

  }

  public record Result(
      @JsonProperty("row")
      List<StationItem> rows
  ) {

  }

  @Builder
  public record StationItem(
      @JsonProperty("BLDN_ID")
      String stationId,

      @JsonProperty("BLDN_NM")
      String name,

      @JsonProperty("ROUTE")
      String line,

      @JsonProperty("LAT")
      Double latitude,

      @JsonProperty("LOT")
      Double longitude
  ) {

  }
}