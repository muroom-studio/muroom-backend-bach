package kr.muroom.muroombackendbach.studio.presentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.studio.application.StudioOptionService;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioOptionResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioOptionResponse.GetAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Studio Option", description = "스튜디오 옵션 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/studios")
public class StudioOptionController {

  private final StudioOptionService studioOptionService;

  @GetMapping("/filter-options")
  public ApiResponse<StudioOptionResponse.GetAll> getAllFilterOptions() {
    GetAll response = studioOptionService.getAllFilterOptions();
    return ApiResponse.success(response);
  }
}
