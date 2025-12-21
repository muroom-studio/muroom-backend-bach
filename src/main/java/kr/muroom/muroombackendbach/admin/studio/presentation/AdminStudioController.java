package kr.muroom.muroombackendbach.admin.studio.presentation;

import kr.muroom.muroombackendbach.admin.studio.application.AdminStudioService;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioImagePresignedUrlRequest;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/studios")
public class AdminStudioController {

  private final AdminStudioService adminStudioService;

  @PostMapping("/presigned-url")
  public ApiResponse<GeneratePresignedPutUrlsResponse> generateStudioImagePresignedUrls(
      @Validated @RequestBody StudioImagePresignedUrlRequest request) {
    GeneratePresignedPutUrlsResponse response = adminStudioService.generatePresignedPutUrls(request);
    return ApiResponse.success(response);
  }

  @PostMapping
  public ApiResponse<Long> createStudio(@Validated @RequestBody StudioCreateRequest request) {
    Long studioId = adminStudioService.createStudio(request);
    return ApiResponse.success(studioId);
  }
}
