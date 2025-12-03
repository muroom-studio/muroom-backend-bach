package kr.muroom.muroombackendbach.map.application;

import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;
import org.springframework.stereotype.Service;

@Service
public class CoordinateTransformService {

  // 대한민국에서 자주 쓰는 좌표계 정의
  // EPSG:5179 (GRS80, UTM-K) -> 네이버 지도, 공공데이터 등에서 많이 씀 (X, Y 좌표)
  private static final String GRS80_5179 = "+proj=tmerc +lat_0=38 +lon_0=127.5 +k=0.9996 +x_0=1000000 +y_0=2000000 +ellps=GRS80 +units=m "
      + "+no_defs";

  // EPSG:4326 (WGS84) -> 구글 지도, GPS, 일반적인 위경도
  private static final String WGS84_4326 = "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs";

  public Coordinate transformGRS80toWGS84(double x, double y) {
    CRSFactory crsFactory = new CRSFactory();

    // 1. 소스 좌표계 (GRS80 - EPSG:5179 라고 가정)
    // 만약 5174, 5181 등 다른 좌표계라면 Proj4 문자열만 바꾸면 됩니다.
    CoordinateReferenceSystem srcCrs = crsFactory.createFromParameters("EPSG:5179", GRS80_5179);

    // 2. 타겟 좌표계 (WGS84 - EPSG:4326)
    CoordinateReferenceSystem targetCrs = crsFactory.createFromParameters("EPSG:4326", WGS84_4326);

    // 3. 변환 객체 생성
    CoordinateTransform transform = new BasicCoordinateTransform(srcCrs, targetCrs);

    // 4. 변환 실행
    ProjCoordinate srcCoord = new ProjCoordinate(x, y);
    ProjCoordinate targetCoord = new ProjCoordinate();

    transform.transform(srcCoord, targetCoord);

    // 결과 반환 (x=경도(longitude), y=위도(latitude))
    // 주의: WGS84는 보통 (위도, 경도) 순서로 쓰지만, 좌표계상 x는 경도입니다.
    return new Coordinate(targetCoord.x, targetCoord.y);
  }

  // 결과를 담을 간단한 DTO (또는 Point 객체 사용)
  public record Coordinate(double longitude, double latitude) {

  }
}