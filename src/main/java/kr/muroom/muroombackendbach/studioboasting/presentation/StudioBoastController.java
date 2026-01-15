package kr.muroom.muroombackendbach.studioboasting.presentation;

import kr.muroom.muroombackendbach.auth.annotation.CurrentUserId;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.report.application.ReportService;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.presentation.dto.request.RegisterReportRequest;
import kr.muroom.muroombackendbach.studioboasting.application.StudioBoastLikeService;
import kr.muroom.muroombackendbach.studioboasting.application.StudioBoastService;
import kr.muroom.muroombackendbach.studioboasting.presentation.docs.StudioBoastControllerDocs;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.CreateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.StudioBoastImageUploadRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.UpdateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastSimpleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/studio-boasts")
public class StudioBoastController implements StudioBoastControllerDocs {

  private final StudioBoastService studioBoastService;
  private final StudioBoastLikeService studioBoastLikeService;
  private final ReportService reportService;

  private final static String SORT_KEY_RANDOM = "random";

  @PostMapping("/presigned-url")
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<GeneratePresignedPutUrlResponse> generateStudioBoastImagePresignedUrls(
      @Validated @RequestBody StudioBoastImageUploadRequest request) {
    GeneratePresignedPutUrlResponse response =
        studioBoastService.generateStudioImagePresignedPutUrl(
            request);
    return ApiResponse.success(response);
  }

  @PostMapping
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<String> createStudioBoast(
      @Validated @RequestBody CreateStudioBoastRequest request,
      @CurrentUserId Long musicianId
  ) {
    Long newStudioBoastId = studioBoastService.createStudioBoast(request, musicianId);
    String response = String.valueOf(newStudioBoastId);
    return ApiResponse.success(response);
  }

  @PutMapping("/{studioBoastId}")
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<String> updateStudioBoast(
      @PathVariable Long studioBoastId,
      @Validated @RequestBody UpdateStudioBoastRequest request,
      @CurrentUserId Long musicianId
  ) {
    Long updatedStudioBoastId = studioBoastService.updateStudioBoast(studioBoastId, request,
        musicianId);
    String response = String.valueOf(updatedStudioBoastId);
    return ApiResponse.success(response);
  }

  @GetMapping("/{studioBoastId}")
  public ApiResponse<StudioBoastDetailResponse> getStudioBoastDetail(
      @PathVariable Long studioBoastId, @CurrentUserId Long musicianId
  ) {
    StudioBoastDetailResponse response = studioBoastService.getStudioBoastDetail(studioBoastId,
        musicianId);
    return ApiResponse.success(response);
  }

  @GetMapping
  public ApiResponse<PaginatedData<StudioBoastDetailResponse>> getDetailedStudioBoasts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "2") int size,
      @RequestParam(name = "sort", defaultValue = "latest,desc") String sort,
      @CurrentUserId Long musicianId
  ) {
    String sortKey = sort.split(",")[0];

    if (SORT_KEY_RANDOM.equalsIgnoreCase(sortKey)) {
      Pageable pageable = PageRequest.of(page, size);
      Page<StudioBoastDetailResponse> response = studioBoastService.getRandomDetailedStudioBoasts(
          pageable, musicianId);
      return ApiResponse.success(PaginatedData.from(response));
    }

    Pageable pageable = buildStudioBoastPageable(page, size, sort);
    Page<StudioBoastDetailResponse> response = studioBoastService.getDetailedStudioBoasts(pageable,
        musicianId);
    return ApiResponse.success(PaginatedData.from(response));
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<PaginatedData<StudioBoastDetailResponse>> getMyDetailedStudioBoasts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "2") int size,
      @RequestParam(name = "sort", defaultValue = "latest,desc") String sort,
      @CurrentUserId Long musicianId
  ) {
    Pageable pageable = buildStudioBoastPageable(page, size, sort);
    Page<StudioBoastDetailResponse> response = studioBoastService.getMyStudioBoasts(pageable,
        musicianId);
    return ApiResponse.success(PaginatedData.from(response));
  }

  @GetMapping("/simple")
  public ApiResponse<PaginatedData<StudioBoastSimpleResponse>> getSimpleStudioBoasts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(name = "sort", defaultValue = "latest,desc") String sort
  ) {
    Pageable pageable = buildStudioBoastPageable(page, size, sort);
    Page<StudioBoastSimpleResponse> response = studioBoastService.getSimpleStudioBoasts(pageable);
    return ApiResponse.success(PaginatedData.from(response));
  }

  @DeleteMapping("/{studioBoastId}")
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<Void> deleteStudioBoast(
      @PathVariable Long studioBoastId,
      @CurrentUserId Long musicianId
  ) {
    studioBoastService.deleteStudioBoast(studioBoastId, musicianId);
    return ApiResponse.deleted();
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @PostMapping("/{studioBoastId}/report")
  public ApiResponse<Void> reportStudioBoast(
      @PathVariable Long studioBoastId,
      @CurrentUserId Long musicianId,
      @RequestBody RegisterReportRequest request) {
    reportService.registerReport(ReportDomainType.STUDIO_BOAST, studioBoastId, musicianId, request);
    return ApiResponse.success();
  }

  private Pageable buildStudioBoastPageable(int page, int size, String sort) {
    String[] sortParams = sort.split(",");
    String sortKey = sortParams[0];
    Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc"))
        ? Sort.Direction.ASC : Sort.Direction.DESC;

    String property = "createdAt";
    if ("likes".equalsIgnoreCase(sortKey)) {
      property = "likeCount";
    }

    Sort sortOrder = Sort.by(new Sort.Order(direction, property), Sort.Order.desc("id"));
    return PageRequest.of(page, size, sortOrder);
  }

  @PostMapping("/{studioBoastId}/likes")
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<Void> likeStudioBoast(
      @PathVariable Long studioBoastId,
      @CurrentUserId Long musicianId
  ) {
    studioBoastLikeService.likeStudioBoast(studioBoastId, musicianId);
    return ApiResponse.success();
  }

  @DeleteMapping("/{studioBoastId}/likes")
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<Void> unlikeStudioBoast(
      @PathVariable Long studioBoastId,
      @CurrentUserId Long musicianId
  ) {
    studioBoastLikeService.unlikeStudioBoast(studioBoastId, musicianId);
    return ApiResponse.success();
  }
}
