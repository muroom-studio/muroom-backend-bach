package kr.muroom.muroombackendbach.admin.subway;

import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.subway.application.SubwayDataBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/subway")
public class AdminSubwayStationController {

  private final SubwayDataBatchService subwayDataBatchService;

  @PostMapping("/sync")
  public ApiResponse<Integer> syncSubwayStations() {
    int response = subwayDataBatchService.fetchAndSaveSubwayStations();
    return ApiResponse.success(response);
  }
}
