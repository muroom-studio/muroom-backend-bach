package kr.muroom.muroombackendbach.subway.application;

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
    SeoulSubwayStationApiDto.Response response;
    try {
      response = seoulSubwayClient.getSubwayStations(apiKey);
    } catch (Exception e) {
      throw new ExternalApiException("서울시 지하철역 API 호출에 실패했습니다.",
          "DATA.SEOUL.GO.KR/SUBWAY_STATION_MASTER_API", e);
    }

    if (response == null || response.result() == null || response.result().rows() == null) {
      throw new ExternalApiException("서울시 지하철역 API 응답이 유효하지 않습니다.",
          "DATA.SEOUL.GO.KR/SUBWAY_STATION_MASTER_API");
    }

    Map<String, SubwayLine> existingLineMap = subwayLineRepository.findAll().stream()
        .collect(Collectors.toMap(SubwayLine::getName, Function.identity()));

    Map<String, SubwayStation> existingStationMap = subwayStationRepository.findAll().stream()
        .collect(Collectors.toMap(SubwayStation::getName, Function.identity()));

    int processedCount = 0;

    for (SeoulSubwayStationApiDto.StationItem item : response.result().rows()) {
      if (item.latitude() == null || item.longitude() == null) {
        continue;
      }

      String apiRouteName = item.line();

      SubwayLine subwayLine = existingLineMap.get(apiRouteName);
      if (subwayLine == null) {
        String color = SubwayLineColor.findColorByRouteName(apiRouteName);
        SubwayLine newLine = SubwayLine.builder()
            .name(apiRouteName)
            .color(color)
            .description(apiRouteName)
            .build();

        subwayLine = subwayLineRepository.save(newLine);
        existingLineMap.put(apiRouteName, subwayLine);
      }

      String stationName = item.name();
      Point location = geometryFactory.createPoint(
          new Coordinate(item.longitude(), item.latitude())
      );

      SubwayStation subwayStation = existingStationMap.get(stationName);

      if (subwayStation == null) {
        subwayStation = SubwayStation.builder()
            .name(stationName)
            .location(location)
            .build();
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
