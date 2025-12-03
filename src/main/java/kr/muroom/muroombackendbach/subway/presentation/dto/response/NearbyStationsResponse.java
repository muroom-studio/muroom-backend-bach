package kr.muroom.muroombackendbach.subway.presentation.dto.response;

import java.util.List;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayLineInfo;
import lombok.Getter;

@Getter
public class NearbyStationsResponse {

  private final List<StationInfo> stations;

  public NearbyStationsResponse(List<StationInfo> stations) {
    this.stations = stations;
  }

  @Getter
  public static class StationInfo {

    private final Long stationId;
    private final String stationName;
    private final List<StudioSubwayLineInfo> lines;
    private final Double distanceMeters; // 중심 주소로부터의 거리 (미터)

    public StationInfo(Long stationId, String stationName, List<StudioSubwayLineInfo> lines, Double distanceMeters) {
      this.stationId = stationId;
      this.stationName = stationName;
      this.lines = lines;
      this.distanceMeters = Math.round(distanceMeters * 10) / 10.0; // 소수점 첫째 자리까지 반올림
    }
  }
}