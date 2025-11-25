package kr.muroom.muroombackendbach.map.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KakaoDirectionsResponse(
    @JsonProperty("trans_id") String transId,
    List<Route> routes
) {

  public record Route(
      @JsonProperty("result_code") int resultCode,
      @JsonProperty("result_msg") String resultMsg,
      Summary summary
  ) {

  }

  public record Summary(
      @JsonProperty("duration") int duration // 총 소요 시간 (초 단위)
  ) {

  }
}