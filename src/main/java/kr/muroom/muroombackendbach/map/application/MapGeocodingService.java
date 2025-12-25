package kr.muroom.muroombackendbach.map.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.common.exception.ExternalApiException;
import kr.muroom.muroombackendbach.map.infrastructure.client.JusoApiClient;
import kr.muroom.muroombackendbach.map.presentation.dto.JusoCoordResponse;
import kr.muroom.muroombackendbach.map.presentation.dto.JusoSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapGeocodingService {

  private final JusoApiClient jusoApiClient;
  private final GeometryFactory geometryFactory;
  private final CoordinateTransformService coordinateTransformService;

  @Value("${juso.api.search-key}")
  private String jusoSearchKey;

  @Value("${juso.api.coord-key}")
  private String jusoCoordKey;

  private final static double EARTH_RADIUS_KM = 6371.0;

  public Integer calculateDistanceInMeters(Point pointA, Point pointB) {
    if (pointA == null || pointB == null) {
      return null;
    }
    double lon1 = pointA.getX();
    double lat1 = pointA.getY();
    double lon2 = pointB.getX();
    double lat2 = pointB.getY();

    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = EARTH_RADIUS_KM * c * 1000; // Convert to meters

    return (int) Math.round(distance);
  }

  public Point getPointFromAddress(String address) {
    if (address == null || address.isBlank()) {
      throw new IllegalArgumentException("주소는 null 또는 빈 문자열일 수 없습니다.");
    }
    // 1단계: 도로명주소 검색 API 호출로 주소 식별 코드 획득
    JusoSearchResponse searchResponse = jusoApiClient.searchAddress(jusoSearchKey, address, "json", 1, 1);

    // TODO: 중복 코드
    if (searchResponse == null || !"0".equals(searchResponse.results().common().errorCode()) || searchResponse.results().juso() == null
        || searchResponse.results().juso().isEmpty()) {
      throw new ExternalApiException("도로명주소 검색 API 호출에 실패했습니다: " + address, "JUSO.GO.KR/SEARCH");
    }

    JusoSearchResponse.Juso firstResult = searchResponse.results().juso().getFirst();

    // 2단계: 획득한 주소 식별 코드로 주소좌표변환 API 호출
    JusoCoordResponse coordResponse = jusoApiClient.getCoordinates(
        jusoCoordKey,
        firstResult.admCd(),
        firstResult.rnMgtSn(),
        firstResult.udrtYn(),
        firstResult.buldMnnm(),
        firstResult.buldSlno(),
        "json"
    );

    if (coordResponse == null || !"0".equals(coordResponse.results().common().errorCode()) || coordResponse.results().juso() == null
        || coordResponse.results().juso().isEmpty()) {
      throw new ExternalApiException("주소좌표변환 API 호출에 실패했습니다: " + address, "JUSO.GO.KR/COORD");
    }

    // 3단계: GRS80 좌표를 WGS84(위도/경도)로 변환
    JusoCoordResponse.Juso coordResult = coordResponse.results().juso().getFirst();
    CoordinateTransformService.Coordinate coordinate = coordinateTransformService.transformGRS80toWGS84(
        Double.parseDouble(coordResult.entX()),
        Double.parseDouble(coordResult.entY())
    );
    double longitude = BigDecimal.valueOf(coordinate.longitude())
        .setScale(6, RoundingMode.HALF_UP)
        .doubleValue();

    double latitude = BigDecimal.valueOf(coordinate.latitude())
        .setScale(6, RoundingMode.HALF_UP)
        .doubleValue();

    // 3단계: GeometryFactory를 사용하여 Point 객체 생성
    return geometryFactory.createPoint(new Coordinate(longitude, latitude));
  }

  /**
   * 여러 주소를 병렬로 처리하여 좌표로 변환합니다.
   *
   * @param addresses 주소 문자열 리스트
   * @return Key: 원래 주소, Value: 변환된 Point 객체를 담은 Map
   */
  public Map<String, Point> getPointsFromAddresses(List<String> addresses) {
    if (addresses == null || addresses.isEmpty()) {
      return Collections.emptyMap();
    }

    return addresses.parallelStream() // 병렬 스트림으로 동시 처리
        .map(address -> {
          Point point = getPointFromAddress(address);
          return Map.entry(address, point);
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue)); // 동시성 처리에 안전한 ConcurrentMap으로 수집
  }
}