package kr.muroom.muroombackendbach.studioboasting.presentation;

import kr.muroom.muroombackendbach.auth.annotation.CurrentUserId;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.report.application.ReportService;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.presentation.dto.request.RegisterReportRequest;
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
@RequestMapping("/api/v1/studio-boasts/{studioBoastId}/comments")
public class StudioBoastCommentController implements StudioBoastCommentControllerDocs {

  private final StudioBoastCommentService studioBoastCommentService;
  private final ReportService reportService;

  @Override
  @PostMapping
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<String> createComment(
      @PathVariable Long studioBoastId,
      @CurrentUserId Long musicianId,
      @Validated @RequestBody CreateStudioBoastCommentRequest request
  ) {
    Long commentId = studioBoastCommentService.createComment(studioBoastId, musicianId, request);
    return ApiResponse.success(commentId.toString());
  }

  @Override
  @GetMapping
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<PaginatedData<StudioBoastCommentResponse>> getComments(
      @PathVariable Long studioBoastId,
      @CurrentUserId Long musicianId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    // 정렬 순서: 먼저 작성된 댓글이 먼저 보임
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
    Page<StudioBoastCommentResponse> response = studioBoastCommentService.getComments(studioBoastId,
        musicianId, pageable);
    return ApiResponse.success(PaginatedData.from(response));
  }

  @Override
  @PutMapping("/{commentId}")
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<Void> updateComment(
      @PathVariable Long commentId,
      @CurrentUserId Long musicianId,
      @Validated @RequestBody UpdateStudioBoastCommentRequest request
  ) {
    studioBoastCommentService.updateComment(commentId, request, musicianId);
    return ApiResponse.success();
  }

  @Override
  @DeleteMapping("/{commentId}")
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<Void> deleteComment(
      @PathVariable Long commentId,
      @CurrentUserId Long musicianId
  ) {
    studioBoastCommentService.deleteComment(commentId, musicianId);
    return ApiResponse.deleted();
  }

  @PostMapping("/{commentId}/likes")
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<Void> likeComment(
      @PathVariable Long commentId,
      @CurrentUserId Long musicianId
  ) {
    studioBoastCommentService.likeComment(commentId, musicianId);
    return ApiResponse.success();
  }

  @DeleteMapping("/{commentId}/likes")
  @PreAuthorize("hasRole('MUSICIAN')")
  public ApiResponse<Void> unlikeComment(
      @PathVariable Long commentId,
      @CurrentUserId Long musicianId
  ) {
    studioBoastCommentService.unlikeComment(commentId, musicianId);
    return ApiResponse.success();
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @PostMapping("/{commentId}/report")
  public ApiResponse<Void> reportComment(
      @PathVariable Long commentId,
      @CurrentUserId Long musicianId,
      @RequestBody RegisterReportRequest request) {
    reportService.registerReport(ReportDomainType.STUDIO_BOAST_COMMENT, commentId,
        musicianId, request);
    return ApiResponse.success();
  }
}
