package kr.muroom.muroombackendbach.studio.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.studio.application.StudioService;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioRequest;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioResponse.MapBoundsSearch;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioResponse.MapList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/studios")
public class StudioController {

  private final StudioService studioService;

  @GetMapping("/map-search")
  public ApiResponse<List<StudioResponse.MapBoundsSearch>> searchStudiosInMapBounds(
      @ModelAttribute StudioRequest.MapBoundsSearch request
  ) {
    List<MapBoundsSearch> response = studioService.searchStudiosInMapBounds(request);
    return ApiResponse.success(response);
  }

  @Operation(summary = "지도 기반 스튜디오 목록 페이지네이션 조회",
      description = "지도 범위 내의 스튜디오 목록을 페이지네이션하여 조회합니다. 기본 정렬은 가격 오름차순입니다.")
  @GetMapping("/map-list")
  public ApiResponse<PaginatedData<StudioResponse.MapList>> searchStudiosForMapList(
      @Parameter(description = "지도 검색 범위 (최소/최대 위도, 최소/최대 경도)")
      @ModelAttribute StudioRequest.MapBoundsSearch request,
      @Parameter(description = "페이지네이션 정보로 기본 정렬은 가격 오름차순입니다.")
      @PageableDefault Pageable pageable
  ) {
    Page<MapList> response = studioService.searchStudiosForMapList(request, pageable);
    return ApiResponse.success(PaginatedData.from(response));
  }
}
