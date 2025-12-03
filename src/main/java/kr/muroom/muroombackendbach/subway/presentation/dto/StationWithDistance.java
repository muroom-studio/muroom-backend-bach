package kr.muroom.muroombackendbach.subway.presentation.dto;

import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStation;
import lombok.Getter;

@Getter
public class StationWithDistance {

  private final SubwayStation station;
  private final Double distance;

  public StationWithDistance(SubwayStation station, Double distance) {
    this.station = station;
    this.distance = distance;
  }
}