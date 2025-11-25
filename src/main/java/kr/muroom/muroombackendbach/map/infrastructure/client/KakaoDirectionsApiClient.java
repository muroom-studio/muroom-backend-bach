package kr.muroom.muroombackendbach.map.infrastructure.client;

import kr.muroom.muroombackendbach.map.config.KakaoFeignClientConfig;
import kr.muroom.muroombackendbach.map.presentation.dto.KakaoDirectionsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "kakao-directions-api-client",
    url = "https://apis-navi.kakaomobility.com",
    configuration = KakaoFeignClientConfig.class
)
public interface KakaoDirectionsApiClient {

  @GetMapping("/v1/directions")
  KakaoDirectionsResponse getDirections(
      @RequestParam("origin") String origin,
      @RequestParam("destination") String destination
  );
}
