package kr.muroom.muroombackendbach.studioboasting.presentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlsResponse;
import kr.muroom.muroombackendbach.studioboasting.application.StudioBoastService;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.CreateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.StudioBoastImageUploadRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "내 작업실 소개(자랑) API", description = "내 작업실 소개(자랑) 및 해당 컨텐츠 댓글, 좋아요 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/studio-boasts")
public class StudioBoastController {

  private final StudioBoastService studioBoastService;

  @PostMapping("/images/presigned-urls")
  public ApiResponse<GeneratePresignedPutUrlsResponse> generateStudioBoastImagePresignedUrls(
      @Validated @RequestBody List<StudioBoastImageUploadRequest> request) {
    GeneratePresignedPutUrlsResponse response = studioBoastService.generateStudioImagePresignedPutUrls(request);
    return ApiResponse.success(response);
  }

  @PostMapping
  public ApiResponse<Long> createStudioBoast(
      @Validated @RequestBody CreateStudioBoastRequest request) {
    Long studioBoastId = studioBoastService.createStudioBoast(request);
    return ApiResponse.success(studioBoastId);
  }

  @GetMapping("/{studioBoastId}")
  public ApiResponse<StudioBoastDetailResponse> getStudioBoastDetail(@PathVariable Long studioBoastId) {
    StudioBoastDetailResponse response = studioBoastService.getStudioBoastDetail(studioBoastId);
    return ApiResponse.success(response);
  }
  
  @DeleteMapping("/{studioBoastId}")
  public ApiResponse<Void> deleteStudioBoast(@PathVariable Long studioBoastId) {
    studioBoastService.deleteStudioBoast(studioBoastId);
    return ApiResponse.deleted();
  }
}
