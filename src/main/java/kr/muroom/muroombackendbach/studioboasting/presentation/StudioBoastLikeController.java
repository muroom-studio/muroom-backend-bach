package kr.muroom.muroombackendbach.studioboasting.presentation;

import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.studioboasting.application.StudioBoastLikeService;
import kr.muroom.muroombackendbach.studioboasting.presentation.docs.StudioBoastLikeControllerDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/studio-boasts")
public class StudioBoastLikeController implements StudioBoastLikeControllerDocs {

  private final StudioBoastLikeService studioBoastLikeService;

  @PostMapping("/{studioBoastId}/likes")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<Void> likeStudioBoast(
      @PathVariable Long studioBoastId,
      @AuthenticationPrincipal Long musicianId
  ) {
    studioBoastLikeService.likeStudioBoast(studioBoastId, musicianId);
    return ApiResponse.success();
  }

  @DeleteMapping("/{studioBoastId}/likes")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<Void> unlikeStudioBoast(
      @PathVariable Long studioBoastId,
      @AuthenticationPrincipal Long musicianId
  ) {
    studioBoastLikeService.unlikeStudioBoast(studioBoastId, musicianId);
    return ApiResponse.success();
  }
}
