package kr.muroom.muroombackendbach.map.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// 주소좌표변환 API 응답 DTO
public record JusoCoordResponse(
    @JsonProperty("results")
    Results results
) {

  public record Results(
      @JsonProperty("common")
      Common common,
      @JsonProperty("juso")
      List<Juso> juso
  ) {

  }

  public record Common(
      @JsonProperty("errorCode") String errorCode,
      @JsonProperty("errorMessage") String errorMessage
  ) {

  }

  public record Juso(
      @JsonProperty("entX") String entX, // 경도
      @JsonProperty("entY") String entY  // 위도
  ) {

  }
}