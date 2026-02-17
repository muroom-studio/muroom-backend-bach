package kr.muroom.muroombackendbach.studio.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import kr.muroom.muroombackendbach.auth.annotation.CurrentUserId;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.studio.application.StudioService;
import kr.muroom.muroombackendbach.studio.application.query.StudioQueryService;
import kr.muroom.muroombackendbach.studio.presentation.dto.request.MapSearchRequest;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioAddressSearchResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDetailResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioListElementResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioMapResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/studios")
public class StudioController {

  private final StudioService studioService;
  private final StudioQueryService studioQueryService;

  @GetMapping("/map-search")
  public ApiResponse<List<StudioMapResponse>> searchStudiosInMapBounds(
      @Validated @ParameterObject MapSearchRequest request
  ) {
    List<StudioMapResponse> response = studioService.searchStudiosInMapBounds(request);
    return ApiResponse.success(response);
  }

  @Operation(summary = "지도 기반 스튜디오 목록 페이지네이션 조회",
      description = "지도 범위 내의 스튜디오 목록을 페이지네이션하여 조회합니다. 기본 정렬은 최신순입니다.",
      parameters = {
          @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
          @Parameter(name = "size", description = "페이지 당 항목 수", example = "10"),
          @Parameter(name = "sort", description = "정렬 기준 (예: 'price,asc', 'latest,desc'). 생략 시 "
              + "기본값은 'latest,desc' (최신순) 입니다.",
              example = "price,desc")
      }
  )
  @GetMapping("/map-list")
  public ApiResponse<PaginatedData<StudioListElementResponse>> searchStudiosForMapList(
      @Validated @ParameterObject MapSearchRequest request,
      @CurrentUserId(required = false) Long musicianId,
      @Parameter(hidden = true)
      @PageableDefault(sort = "latest", direction = Direction.DESC) Pageable pageable
  ) {
    Page<StudioListElementResponse> response = studioService.searchStudiosForMapList(request,
        musicianId, pageable);
    return ApiResponse.success(PaginatedData.from(response));
  }

  @Operation(summary = "스튜디오 상세 조회", description = "스튜디오의 상세 정보를 조회합니다.")
  @GetMapping("/{studioId}")
  public ApiResponse<StudioDetailResponse> getStudio(@PathVariable Long studioId,
      @CurrentUserId(required = false) Long musicianId) {
    StudioDetailResponse response = studioQueryService.getStudio(studioId, musicianId);

    return ApiResponse.success(response);
  }

  @Operation(summary = "도로명 주소로 스튜디오 검색", description = "입력한 도로명 주소(일부분 가능)를 포함하는 스튜디오들을 검색합니다.")
  @GetMapping("/search/address")
  public ApiResponse<List<StudioAddressSearchResponse>> searchStudiosByAddress(
      @Parameter(description = "도로명 주소(일부분도 가능)") @RequestParam @NotBlank String roadNameAddress
  ) {
    List<StudioAddressSearchResponse> response = studioService.getStudiosByRoadNameAddress(
        roadNameAddress);
    return ApiResponse.success(response);
  }
}
