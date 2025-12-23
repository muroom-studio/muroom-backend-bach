package kr.muroom.muroombackendbach.studioboasting.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.studioboasting.application.StudioBoastService;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.CreateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.StudioBoastImageUploadRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.UpdateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastListElementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "내 작업실 소개(자랑) API", description = "내 작업실 소개(자랑) 및 해당 컨텐츠 댓글, 좋아요 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/studio-boasts")
public class StudioBoastController {

  private final StudioBoastService studioBoastService;

  // TODO: @PreAuthorize("isAuthenticated()") 추가 필요

  @PostMapping("/presigned-url")
  public ApiResponse<GeneratePresignedPutUrlResponse> generateStudioBoastImagePresignedUrls(
      @Validated @RequestBody StudioBoastImageUploadRequest request) {
    GeneratePresignedPutUrlResponse response = studioBoastService.generateStudioImagePresignedPutUrl(request);
    return ApiResponse.success(response);
  }

  @PostMapping
  public ApiResponse<Long> createStudioBoast(
      @Validated @RequestBody CreateStudioBoastRequest request) {
    Long response = studioBoastService.createStudioBoast(request);
    return ApiResponse.success(response);
  }

  @PutMapping("/{studioBoastId}")
  public ApiResponse<Long> updateStudioBoast(
      @PathVariable Long studioBoastId,
      @Validated @RequestBody UpdateStudioBoastRequest request,
      @AuthenticationPrincipal Long musicianId
  ) {
    Long response = studioBoastService.updateStudioBoast(studioBoastId, request, musicianId);
    return ApiResponse.success(response);
  }

  @GetMapping("/{studioBoastId}")
  public ApiResponse<StudioBoastDetailResponse> getStudioBoastDetail(@PathVariable Long studioBoastId) {
    StudioBoastDetailResponse response = studioBoastService.getStudioBoastDetail(studioBoastId);
    return ApiResponse.success(response);
  }

  @Operation(summary = "작업실 자랑 게시글 목록 페이지네이션 조회",
      description = "작업실 자랑 게시글 목록을 페이지네이션하여 조회합니다. 기본 정렬은 최신순입니다.",
      parameters = {
          @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
          @Parameter(name = "size", description = "페이지 당 항목 수", example = "12"),
          @Parameter(name = "sort", description = "정렬 기준 (예: 'likes,desc', 'latest,desc'). 생략 시 "
              + "기본값은 'latest,desc' (최신순) 입니다.",
              example = "likes,desc")
      }
  )
  @GetMapping
  public ApiResponse<PaginatedData<StudioBoastListElementResponse>> getStudioBoasts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(name = "sort", defaultValue = "latest,desc") String sort
  ) {
    String[] sortParams = sort.split(",");
    String sortKey = sortParams[0];
    Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc"))
        ? Sort.Direction.ASC : Sort.Direction.DESC;

    String property = "createdAt";
    if ("likes".equalsIgnoreCase(sortKey)) {
      property = "likeCount";
    }

    Sort sortOrder = Sort.by(new Sort.Order(direction, property), Sort.Order.desc("id"));
    Pageable pageable = PageRequest.of(page, size, sortOrder);
    Page<StudioBoastListElementResponse> response = studioBoastService.getStudioBoasts(pageable);

    return ApiResponse.success(PaginatedData.from(response));
  }

  @DeleteMapping("/{studioBoastId}")
  public ApiResponse<Void> deleteStudioBoast(@PathVariable Long studioBoastId) {
    studioBoastService.deleteStudioBoast(studioBoastId);
    return ApiResponse.deleted();
  }
}
