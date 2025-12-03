package kr.muroom.muroombackendbach.subway.presentation;

import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.subway.application.SubwayService;
import kr.muroom.muroombackendbach.subway.presentation.dto.response.NearbyStationsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subway")
public class SubwayController {

  private final SubwayService subwayService;

  @GetMapping("/nearby")
  public ApiResponse<NearbyStationsResponse> getNearbySubwayStations(@RequestParam String address) {
    NearbyStationsResponse response = subwayService.findNearbyStations(address);
    return ApiResponse.success(response);
  }
}
