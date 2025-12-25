package kr.muroom.muroombackendbach.subway.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.map.application.MapGeocodingService;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayLineInfo;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayStationLineRepository;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayStationRepository;
import kr.muroom.muroombackendbach.subway.presentation.dto.StationWithDistance;
import kr.muroom.muroombackendbach.subway.presentation.dto.response.NearbyStationsResponse;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubwayService {

  private final SubwayStationRepository subwayStationRepository;
  private final SubwayStationLineRepository subwayStationLineRepository;
  private final MapGeocodingService mapGeocodingService;
  private static final int SEARCH_RADIUS_METERS = 2000; // 2km 반경

  public NearbyStationsResponse findNearbyStations(String address) {
    // 1. 주소를 좌표로 변환
    Point location = mapGeocodingService.getPointFromAddress(address);

    // 2. 좌표 기준 반경 내 지하철역과 거리를 함께 조회
    List<StationWithDistance> stationsWithDistance = subwayStationRepository.findNearbyStationsWithDistance(location, SEARCH_RADIUS_METERS);

    // 3. 지하철역들의 노선 정보 일괄 조회 (N+1 방지)
    List<Long> stationIds = stationsWithDistance.stream().map(s -> s.getStation().getId()).toList();
    Map<Long, List<StudioSubwayLineInfo>> linesByStationId = subwayStationLineRepository.findAllByStationIdsInWithLine(stationIds)
        .stream()
        .collect(Collectors.groupingBy(
            ssl -> ssl.getStation().getId(),
            Collectors.mapping(ssl -> new StudioSubwayLineInfo(ssl.getLine().getName(), ssl.getLine().getColor()), Collectors.toList())
        ));

    // 4. 최종 응답 DTO로 조립
    List<NearbyStationsResponse.StationInfo> stationInfos = stationsWithDistance.stream()
        .map(swd -> new NearbyStationsResponse.StationInfo(
            String.valueOf(swd.getStation().getId()),
            swd.getStation().getName(),
            linesByStationId.getOrDefault(swd.getStation().getId(), List.of()),
            swd.getDistance().intValue()
        ))
        .toList();

    return new NearbyStationsResponse(stationInfos);
  }

  /**
   * 여러 주소를 바탕으로 인근 지하철역 정보를 일괄 조회합니다.
   *
   * @param addresses 주소 문자열 리스트
   * @return Key: 원래 주소, Value: 인근 지하철역 정보 리스트를 담은 Map
   */
  public Map<String, NearbyStationsResponse> findNearbyStationsInBulk(List<String> addresses) {
    // 1. 여러 주소를 한 번에 좌표로 변환 (내부적으로 병렬 처리)
    Map<String, Point> pointsByAddress = mapGeocodingService.getPointsFromAddresses(addresses);
    if (pointsByAddress.isEmpty()) {
      return Collections.emptyMap();
    }

    // 결과를 담을 Map 초기화
    Map<String, NearbyStationsResponse> result = new HashMap<>();

    // 2. 각 좌표별로 DB에서 인근 지하철역 조회
    // !!주의!!: 이 부분은 DB 조회를 반복하지만, 외부 API 호출에 비하면 훨씬 빠릅니다.
    // 극단적인 최적화가 필요하다면 이 부분도 하나의 쿼리로 합칠 수 있으나, 현재는 API 문제 해결에 집중합니다.
    pointsByAddress.forEach((address, point) -> {
      List<StationWithDistance> stationsWithDistance = subwayStationRepository.findNearbyStationsWithDistance(point, SEARCH_RADIUS_METERS);

      // 기존 findNearbyStations의 로직을 재사용합니다.
      List<Long> stationIds = stationsWithDistance.stream().map(s -> s.getStation().getId()).toList();
      Map<Long, List<StudioSubwayLineInfo>> linesByStationId = subwayStationLineRepository.findAllByStationIdsInWithLine(stationIds)
          .stream()
          .collect(Collectors.groupingBy(
              ssl -> ssl.getStation().getId(),
              Collectors.mapping(ssl -> new StudioSubwayLineInfo(ssl.getLine().getName(), ssl.getLine().getColor()), Collectors.toList())
          ));

      List<NearbyStationsResponse.StationInfo> stationInfos = stationsWithDistance.stream()
          .map(swd -> new NearbyStationsResponse.StationInfo(
              String.valueOf(swd.getStation().getId()),
              swd.getStation().getName(),
              linesByStationId.getOrDefault(swd.getStation().getId(), List.of()),
              swd.getDistance().intValue()
          ))
          .toList();

      result.put(address, new NearbyStationsResponse(stationInfos));
    });

    return result;
  }
}