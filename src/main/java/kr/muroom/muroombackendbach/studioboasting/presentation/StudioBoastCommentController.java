package kr.muroom.muroombackendbach.studioboasting.presentation;

import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.studioboasting.application.StudioBoastCommentService;
import kr.muroom.muroombackendbach.studioboasting.presentation.docs.StudioBoastCommentControllerDocs;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.CreateStudioBoastCommentRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.UpdateStudioBoastCommentRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastCommentResponse;
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
public class StudioBoastCommentController implements StudioBoastCommentControllerDocs {

  private StudioBoastCommentService studioBoastCommentService;

  @Override
  @PostMapping("/{studioBoastId}/comments")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<Long> createComment(
      @PathVariable Long studioBoastId,
      @AuthenticationPrincipal Long musicianId,
      @Validated @RequestBody CreateStudioBoastCommentRequest request
  ) {
    Long commentId = studioBoastCommentService.createComment(studioBoastId, musicianId, request);
    return ApiResponse.success(commentId);
  }

  @Override
  @GetMapping("/{studioBoastId}/comments")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<PaginatedData<StudioBoastCommentResponse>> getComments(
      @PathVariable Long studioBoastId,
      @AuthenticationPrincipal Long musicianId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    // 최신순 정렬 고정
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<StudioBoastCommentResponse> response = studioBoastCommentService.getComments(studioBoastId, musicianId, pageable);
    return ApiResponse.success(PaginatedData.from(response));
  }

  @Override
  @PutMapping("/comments/{commentId}")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<Void> updateComment(
      @PathVariable Long commentId,
      @AuthenticationPrincipal Long musicianId,
      @Validated @RequestBody UpdateStudioBoastCommentRequest request
  ) {
    studioBoastCommentService.updateComment(commentId, request.content(), musicianId);
    return ApiResponse.success();
  }

  @Override
  @DeleteMapping("/comments/{commentId}")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<Void> deleteComment(
      @PathVariable Long commentId,
      @AuthenticationPrincipal Long musicianId
  ) {
    studioBoastCommentService.deleteComment(commentId, musicianId);
    return ApiResponse.deleted();
  }

  @PostMapping("/comments/{commentId}/likes")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<Void> likeComment(
      @PathVariable Long commentId,
      @AuthenticationPrincipal Long musicianId
  ) {
    studioBoastCommentService.likeComment(commentId, musicianId);
    return ApiResponse.success();
  }

  @DeleteMapping("/comments/{commentId}/likes")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<Void> unlikeComment(
      @PathVariable Long commentId,
      @AuthenticationPrincipal Long musicianId
  ) {
    studioBoastCommentService.unlikeComment(commentId, musicianId);
    return ApiResponse.success();
  }
}
