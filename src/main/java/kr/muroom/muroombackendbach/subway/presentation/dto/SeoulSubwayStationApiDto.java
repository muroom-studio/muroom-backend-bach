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
      @JsonProperty("list_total_count") // 전체 데이터 수 필드 추가
      Integer listTotalCount,
      @JsonProperty("RESULT") // API 호출 결과 필드 추가
      ApiResult apiResult,
      @JsonProperty("row")
      List<StationItem> rows
  ) {

  }

  // API 호출 결과 (CODE, MESSAGE)를 위한 새로운 레코드 추가
  public record ApiResult(
      @JsonProperty("CODE")
      String code,
      @JsonProperty("MESSAGE")
      String message
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