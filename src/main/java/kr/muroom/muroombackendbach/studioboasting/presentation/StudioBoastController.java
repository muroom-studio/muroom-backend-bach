package kr.muroom.muroombackendbach.studioboasting.presentation;

import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.studioboasting.application.StudioBoastLikeService;
import kr.muroom.muroombackendbach.studioboasting.application.StudioBoastService;
import kr.muroom.muroombackendbach.studioboasting.presentation.docs.StudioBoastControllerDocs;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.CreateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.StudioBoastImageUploadRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.UpdateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/studio-boasts")
public class StudioBoastController implements StudioBoastControllerDocs {

  private final StudioBoastService studioBoastService;
  private final StudioBoastLikeService studioBoastLikeService;

  @PostMapping("/presigned-url")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<GeneratePresignedPutUrlResponse> generateStudioBoastImagePresignedUrls(
      @Validated @RequestBody StudioBoastImageUploadRequest request) {
    GeneratePresignedPutUrlResponse response = studioBoastService.generateStudioImagePresignedPutUrl(request);
    return ApiResponse.success(response);
  }

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<String> createStudioBoast(
      @Validated @RequestBody CreateStudioBoastRequest request,
      @AuthenticationPrincipal Long musicianId
  ) {
    Long newStudioBoastId = studioBoastService.createStudioBoast(request, musicianId);
    String response = String.valueOf(newStudioBoastId);
    return ApiResponse.success(response);
  }

  @PutMapping("/{studioBoastId}")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<String> updateStudioBoast(
      @PathVariable Long studioBoastId,
      @Validated @RequestBody UpdateStudioBoastRequest request,
      @AuthenticationPrincipal Long musicianId
  ) {
    Long updatedStudioBoastId = studioBoastService.updateStudioBoast(studioBoastId, request, musicianId);
    String response = String.valueOf(updatedStudioBoastId);
    return ApiResponse.success(response);
  }

  @GetMapping("/{studioBoastId}")
  public ApiResponse<StudioBoastDetailResponse> getStudioBoastDetail(
      @PathVariable Long studioBoastId, @AuthenticationPrincipal Long musicianId
  ) {
    StudioBoastDetailResponse response = studioBoastService.getStudioBoastDetail(studioBoastId, musicianId);
    return ApiResponse.success(response);
  }

  @GetMapping
  public ApiResponse<PaginatedData<StudioBoastDetailResponse>> getStudioBoasts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(name = "sort", defaultValue = "latest,desc") String sort,
      @AuthenticationPrincipal Long musicianId
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
    Page<StudioBoastDetailResponse> response = studioBoastService.getStudioBoasts(pageable, musicianId);

    return ApiResponse.success(PaginatedData.from(response));
  }

  @DeleteMapping("/{studioBoastId}")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<Void> deleteStudioBoast(
      @PathVariable Long studioBoastId,
      @AuthenticationPrincipal Long musicianId
  ) {
    studioBoastService.deleteStudioBoast(studioBoastId, musicianId);
    return ApiResponse.deleted();
  }
}
