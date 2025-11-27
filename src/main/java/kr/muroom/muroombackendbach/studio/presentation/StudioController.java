package kr.muroom.muroombackendbach.studio.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.studio.application.StudioService;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioRequest;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioResponse.MapBoundsSearch;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioResponse.MapList;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/studios")
public class StudioController {

  private final StudioService studioService;

  @GetMapping("/map-search")
  public ApiResponse<List<MapBoundsSearch>> searchStudiosInMapBounds(
      @ParameterObject StudioRequest.MapBoundsSearch request
  ) {
    List<MapBoundsSearch> response = studioService.searchStudiosInMapBounds(request);
    return ApiResponse.success(response);
  }

  @Operation(summary = "지도 기반 스튜디오 목록 페이지네이션 조회",
      description = "지도 범위 내의 스튜디오 목록을 페이지네이션하여 조회합니다. 기본 정렬은 최신순입니다.",
      parameters = {
          @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
          @Parameter(name = "size", description = "페이지 당 항목 수", example = "10"),
          @Parameter(name = "sort", description = "정렬 기준 (예: 'price,asc', 'latest,desc'). 생략 시 "
              + "기본값은 'latest,desc' (최신순) 입니다.",
              example = "latest,desc")
      }
  )
  // @SecurityRequirement(name = "Authorization")
  @GetMapping("/map-list")
  public ApiResponse<PaginatedData<MapList>> searchStudiosForMapList(
      @ParameterObject StudioRequest.MapBoundsSearch request,
      @Parameter(hidden = true)
      @PageableDefault(sort = "latest", direction = Direction.DESC) Pageable pageable
  ) {
    Page<MapList> response = studioService.searchStudiosForMapList(request, pageable);
    return ApiResponse.success(PaginatedData.from(response));
  }
}
