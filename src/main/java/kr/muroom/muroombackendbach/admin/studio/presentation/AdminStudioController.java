package kr.muroom.muroombackendbach.admin.studio.presentation;

import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioImageUploadRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioUpdateRequest;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.studio.application.command.StudioCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/studios")
public class AdminStudioController {

  private final StudioCommandService studioCommandService;

  @PostMapping("/presigned-url")
  public ApiResponse<GeneratePresignedPutUrlResponse> generateStudioImagePresignedUrl(
      @Validated @RequestBody StudioImageUploadRequest request) {
    GeneratePresignedPutUrlResponse response = studioCommandService.generatePresignedPutUrl(request);
    return ApiResponse.success(response);
  }

  @PostMapping
  public ApiResponse<Long> createStudio(@Validated @RequestBody StudioCreateRequest request) {
    Long studioId = studioCommandService.createStudio(request);
    return ApiResponse.success(studioId);
  }

  @PutMapping("/{studioId}")
  public ApiResponse<Void> updateStudio(
      @PathVariable Long studioId,
      @Validated @RequestBody StudioUpdateRequest request
  ) {
    studioCommandService.updateStudio(studioId, request);
    return ApiResponse.success();
  }

  @DeleteMapping("/{studioId}")
  public ApiResponse<Void> deleteStudio(@PathVariable Long studioId) {
    studioCommandService.deleteStudio(studioId);
    return ApiResponse.success();
  }
}
