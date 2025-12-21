package kr.muroom.muroombackendbach.subway.application;

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
            swd.getStation().getId(),
            swd.getStation().getName(),
            linesByStationId.getOrDefault(swd.getStation().getId(), List.of()),
            swd.getDistance().intValue()
        ))
        .toList();

    return new NearbyStationsResponse(stationInfos);
  }
}