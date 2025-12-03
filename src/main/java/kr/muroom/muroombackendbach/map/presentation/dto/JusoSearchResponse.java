package kr.muroom.muroombackendbach.map.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record JusoSearchResponse(
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
      @JsonProperty("totalCount") String totalCount,
      @JsonProperty("errorCode") String errorCode,
      @JsonProperty("errorMessage") String errorMessage
  ) {

  }

  // 가장 중요한 주소 정보 DTO
  public record Juso(
      @JsonProperty("roadAddr") String roadAddr,      // 전체 도로명 주소
      @JsonProperty("jibunAddr") String jibunAddr,     // 지번 주소
      @JsonProperty("zipNo") String zipNo,         // 우편번호
      @JsonProperty("admCd") String admCd,           // 행정구역코드
      @JsonProperty("rnMgtSn") String rnMgtSn,       // 도로명코드
      @JsonProperty("udrtYn") String udrtYn,        // 지하여부
      @JsonProperty("buldMnnm") String buldMnnm,      // 건물본번
      @JsonProperty("buldSlno") String buldSlno       // 건물부번
  ) {

  }
}