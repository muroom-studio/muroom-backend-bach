package kr.muroom.muroombackendbach.subway.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.common.exception.ExternalApiException;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayLine;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStation;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayLineRepository;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayStationRepository;
import kr.muroom.muroombackendbach.subway.domain.valueobjects.SubwayLineColor;
import kr.muroom.muroombackendbach.subway.infrastructure.client.SeoulSubwayClient;
import kr.muroom.muroombackendbach.subway.presentation.dto.SeoulSubwayStationApiDto;
import kr.muroom.muroombackendbach.subway.presentation.dto.SeoulSubwayStationApiDto.StationItem;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class SubwayDataBatchService {

  private static final int MAX_FETCH_SIZE = 1000;

  private final SubwayStationRepository subwayStationRepository;
  private final SubwayLineRepository subwayLineRepository;
  private final GeometryFactory geometryFactory;
  private final SeoulSubwayClient seoulSubwayClient;
  private final String apiKey;

  public SubwayDataBatchService(SubwayStationRepository subwayStationRepository,
      SubwayLineRepository subwayLineRepository,
      GeometryFactory geometryFactory,
      SeoulSubwayClient seoulSubwayClient,
      @Value("${seoul.api.key}") String apiKey) {
    this.subwayStationRepository = subwayStationRepository;
    this.subwayLineRepository = subwayLineRepository;
    this.geometryFactory = geometryFactory;
    this.seoulSubwayClient = seoulSubwayClient;
    this.apiKey = apiKey;
  }

  public int fetchAndSaveSubwayStations() {
    // 1. 첫 호출로 전체 데이터 수를 확인합니다.
    SeoulSubwayStationApiDto.Response firstResponse = callSubwayApi(1, 1);
    Integer totalCount = firstResponse.result().listTotalCount();

    if (totalCount == null || totalCount == 0) {
      log.warn("서울시 지하철역 정보가 0건이거나 전체 데이터 수를 가져올 수 없습니다.");
      return 0;
    }

    // 2. 모든 데이터를 담을 리스트를 생성합니다.
    List<SeoulSubwayStationApiDto.StationItem> allItems = new ArrayList<>();

    // 3. 전체 데이터 수만큼 반복하여 API를 호출합니다.
    for (int i = 0; i * MAX_FETCH_SIZE < totalCount; i++) {
      int startIndex = i * MAX_FETCH_SIZE + 1;
      int endIndex = (i + 1) * MAX_FETCH_SIZE;
      if (endIndex > totalCount) {
        endIndex = totalCount;
      }

      log.info("서울시 지하철역 정보 조회 중... ({} - {})", startIndex, endIndex);
      SeoulSubwayStationApiDto.Response response = callSubwayApi(startIndex, endIndex);
      allItems.addAll(response.result().rows());
    }

    // 4. 조회된 모든 데이터를 DB에 저장/업데이트하는 로직 (기존 로직 재사용)
    return processAndSaveItems(allItems);
  }

  private SeoulSubwayStationApiDto.Response callSubwayApi(int startIndex, int endIndex) {
    SeoulSubwayStationApiDto.Response response;
    try {
      response = seoulSubwayClient.getSubwayStations(apiKey, startIndex, endIndex);
    } catch (Exception e) {
      throw new ExternalApiException("서울시 지하철역 API 호출에 실패했습니다.",
          "DATA.SEOUL.GO.KR/SUBWAY_STATION_MASTER_API", e);
    }

    if (response == null || response.result() == null || response.result().rows() == null
        || response.result().apiResult() == null) {
      throw new ExternalApiException("서울시 지하철역 API 응답이 유효하지 않습니다.",
          "DATA.SEOUL.GO.KR/SUBWAY_STATION_MASTER_API");
    }
    if (!"INFO-000".equals(response.result().apiResult().code())) {
      throw new ExternalApiException("서울시 지하철역 API 에러: " + response.result().apiResult().message(),
          "DATA.SEOUL.GO.KR/SUBWAY_STATION_MASTER_API");
    }
    return response;
  }

  private int processAndSaveItems(List<StationItem> items) {
    Map<String, SubwayLine> existingLineMap = subwayLineRepository.findAll().stream()
        .collect(Collectors.toMap(SubwayLine::getName, Function.identity()));

    Map<String, SubwayStation> existingStationMap = subwayStationRepository.findAll().stream()
        .collect(Collectors.toMap(SubwayStation::getName, Function.identity()));

    int processedCount = 0;

    for (SeoulSubwayStationApiDto.StationItem item : items) {
      if (item.latitude() == null || item.longitude() == null) {
        continue;
      }

      String apiRouteName = item.line();
      SubwayLine subwayLine = existingLineMap.computeIfAbsent(apiRouteName, routeName -> {
        String color = SubwayLineColor.findColorByRouteName(routeName);
        SubwayLine newLine = SubwayLine.builder().name(routeName).color(color)
            .description(routeName).build();
        return subwayLineRepository.save(newLine);
      });

      String stationName = item.name();
      Point location = geometryFactory.createPoint(
          new Coordinate(item.longitude(), item.latitude()));

      SubwayStation subwayStation = existingStationMap.get(stationName);
      if (subwayStation == null) {
        subwayStation = SubwayStation.builder().name(stationName).location(location).build();
        existingStationMap.put(stationName, subwayStation);
      } else {
        subwayStation.updateLocation(location);
      }

      subwayStation.addLine(subwayLine);
      processedCount++;
    }

    subwayStationRepository.saveAll(existingStationMap.values());
    log.info("지하철 데이터 동기화 완료: {}건 처리됨", processedCount);
    return processedCount;
  }
}