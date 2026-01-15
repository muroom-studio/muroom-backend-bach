package kr.muroom.muroombackendbach.studioboasting.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.auth.annotation.CurrentUserId;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.CreateStudioBoastCommentRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.UpdateStudioBoastCommentRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastCommentResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "studio boast comment - 작업실 소개(자랑) 댓글 API", description = "게시글 댓글 CRUD 관련 API")
public interface StudioBoastCommentControllerDocs {

  @Operation(summary = "댓글/대댓글 생성", description = "지정된 작업실 자랑 게시글에 댓글 또는 대댓글을 작성합니다.")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200", description = "댓글/대댓글 생성 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400", description = "- `SB-400-12`: 작업실 소개(자랑)글의 대댓글에 대댓글을 달 수 없습니다.\n" +
          "- `SB-400-13`: 부모 댓글이 같은 작업실 소개(자랑)글에 속하지 않습니다.\n" +
          "- `SB-400-91`: 인스타그램 계정 입력 시 이벤트 약관에 동의해야 합니다."),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404", description = "- `SB-404-01`: 해당 작업실 소개(자랑)글을 찾을 수 없습니다.\n" +
          "- `SB-404-11`: 해당 작업실 소개(자랑)글의 댓글을 찾을 수 없습니다.")
  })
  @SecurityRequirement(name = "Authentication")
  @PostMapping
  ApiResponse<String> createComment(
      @PathVariable Long studioBoastId,
      @CurrentUserId Long musicianId,
      @Validated @RequestBody CreateStudioBoastCommentRequest request
  );

  @Operation(summary = "댓글 목록 페이지네이션 조회", description = "지정된 작업실 자랑 게시글의 댓글 목록을 페이지네이션하여 조회합니다. "
      + "(최신순 고정)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200", description = "댓글 목록 페이지네이션 조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404", description = "- `SB-404-01`: 해당 작업실 소개(자랑)글을 찾을 수 없습니다.")
  })
  @SecurityRequirement(name = "Authentication")
  @GetMapping
  ApiResponse<PaginatedData<StudioBoastCommentResponse>> getComments(
      @PathVariable Long studioBoastId,
      @CurrentUserId Long musicianId,
      @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "페이지 당 항목 수", example = "10") @RequestParam(defaultValue = "10") int size
  );

  @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글의 내용을 수정합니다.")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200", description = "댓글 수정 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404", description = "- `SB-404-11`: 해당 작업실 소개(자랑)글의 댓글을 찾을 수 없습니다.")
  })
  @SecurityRequirement(name = "Authentication")
  @PutMapping("/{commentId}")
  ApiResponse<Void> updateComment(
      @PathVariable Long commentId,
      @CurrentUserId Long musicianId,
      @Validated @RequestBody UpdateStudioBoastCommentRequest request
  );

  @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글을 삭제합니다. (Soft Delete)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200", description = "댓글 삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404", description = "- `SB-404-11`: 해당 작업실 소개(자랑)글의 댓글을 찾을 수 없습니다.")
  })
  @SecurityRequirement(name = "Authentication")
  @DeleteMapping("/{commentId}")
  ApiResponse<Void> deleteComment(
      @PathVariable Long commentId,
      @CurrentUserId Long musicianId
  );

  @Operation(summary = "댓글 좋아요", description = "지정된 댓글에 '좋아요'를 누릅니다.")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200", description = "댓글 좋아요 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404", description = "- `SB-404-11`: 해당 작업실 소개(자랑)글의 댓글을 찾을 수 없습니다. (댓글 "
          + "조회 권한이 없는 경우 포함)")
  })
  @SecurityRequirement(name = "Authentication")
  @PostMapping("/{commentId}/likes")
  ApiResponse<Void> likeComment(
      @PathVariable Long commentId,
      @CurrentUserId Long musicianId
  );

  @Operation(summary = "댓글 좋아요 취소", description = "지정된 댓글의 '좋아요'를 취소합니다.")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200", description = "댓글 좋아요 취소 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404", description = "- `SB-404-11`: 해당 작업실 소개(자랑)글의 댓글을 찾을 수 없습니다. (댓글 "
          + "조회 권한이 없는 경우 포함)")
  })
  @SecurityRequirement(name = "Authentication")
  @DeleteMapping("/{commentId}/likes")
  ApiResponse<Void> unlikeComment(
      @PathVariable Long commentId,
      @CurrentUserId Long musicianId
  );
}
