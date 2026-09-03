package kr.muroom.muroombackendbach.map.application;

import java.util.concurrent.CompletableFuture;
import kr.muroom.muroombackendbach.common.exception.ExternalApiException;
import kr.muroom.muroombackendbach.map.infrastructure.client.KakaoDirectionsApiClient;
import kr.muroom.muroombackendbach.map.presentation.dto.KakaoDirectionsResponse;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.scheduling.annotation.Async;

/**
 * Kakao Directions API를 사용한 도보시간 계산 서비스입니다.
 *
 * @deprecated 비용 문제로 더 이상 도보 시간을 사용하지 않으므로 이 서비스는 제거됐습니다.
 */
// @Service
@RequiredArgsConstructor
public class MapDirectionService {

  private final KakaoDirectionsApiClient kakaoDirectionsApiClient;

  /**
   * 두 지점 간의 도보 시간을 분 단위로 비동기 조회합니다.
   *
   * @param start 출발지점 좌표
   * @param end   도착지점 좌표
   * @return 도보 시간(분) 또는 null (조회 실패 시)
   * @deprecated 비용 문제로 더 이상 도보 시간을 사용하지 않으므로 이 메서드는 제거됐습니다.
   */
  @Async
  public CompletableFuture<Integer> getWalkingTimeMinutes(Point start, Point end) {
    if (start == null || end == null) {
      return CompletableFuture.completedFuture(null);
    }

    String origin = String.format("%f,%f", start.getX(), start.getY());
    String destination = String.format("%f,%f", end.getX(), end.getY());

    try {
      KakaoDirectionsResponse response = kakaoDirectionsApiClient.getDirections(origin,
          destination);

      if (response != null && !response.routes().isEmpty()) {
        KakaoDirectionsResponse.Route route = response.routes().getFirst();
        if (route.resultCode() == 0) {
          int durationInSeconds = route.summary().duration();
          return CompletableFuture.completedFuture((int) Math.ceil(durationInSeconds / 60.0));
        }
      }
    } catch (Exception e) {
      throw new ExternalApiException("Kakao Directions API 호출에 실패했습니다.",
          "APIS-NAVI.KAKAOMOBILITY.COM/DIRECTIONS", e);
    }

    return CompletableFuture.completedFuture(null);
  }
}
