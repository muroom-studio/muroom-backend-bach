package kr.muroom.muroombackendbach.map.application;

import java.util.concurrent.CompletableFuture;
import kr.muroom.muroombackendbach.common.exception.ExternalApiException;
import kr.muroom.muroombackendbach.map.infrastructure.client.KakaoDirectionsApiClient;
import kr.muroom.muroombackendbach.map.presentation.dto.KakaoDirectionsResponse;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapDirectionService {

  private final KakaoDirectionsApiClient kakaoDirectionsApiClient;

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
