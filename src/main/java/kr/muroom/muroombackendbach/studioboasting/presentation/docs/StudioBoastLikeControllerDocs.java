package kr.muroom.muroombackendbach.studioboasting.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "studio boast like - 작업실 소개(자랑) 좋아요 API")
public interface StudioBoastLikeControllerDocs {

  @Operation(
      summary = "작업실 소개(자랑) 게시글 좋아요 추가",
      description = "작업실 소개(자랑) 게시글에 좋아요를 추가합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "해당 작업실 소개(자랑) 게시글이 존재하지 않는 경우",
          content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BusinessException.class),
                  examples = {
                      @ExampleObject(
                          name = "작업실 소개(자랑) 게시글 없음",
                          value = """
                              {
                                  "status": 400,
                                  "code": "SB-404-01",
                                  "message": "해당 작업실 소개(자랑)글을 찾을 수 없습니다.",
                              }
                              """
                      )
                  }
              )
          }
      )
  })
  @SecurityRequirement(name = "Authentication")
  @PostMapping("/{studioBoastId}/likes")
  ApiResponse<Void> likeStudioBoast(
      @PathVariable Long studioBoastId,
      @AuthenticationPrincipal Long musicianId
  );

  @Operation(
      summary = "작업실 소개(자랑) 게시글 좋아요 취소",
      description = "작업실 소개(자랑) 게시글에 추가된 좋아요를 취소합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "해당 작업실 소개(자랑) 게시글이 존재하지 않는 경우",
          content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BusinessException.class),
                  examples = {
                      @ExampleObject(
                          name = "작업실 소개(자랑) 게시글 없음",
                          value = """
                              {
                                  "status": 400,
                                  "code": "SB-404-01",
                                  "message": "해당 작업실 소개(자랑)글을 찾을 수 없습니다.",
                              }
                              """
                      )
                  }
              )
          }
      )
  })
  @SecurityRequirement(name = "Authentication")
  @DeleteMapping("/{studioBoastId}/likes")
  ApiResponse<Void> unlikeStudioBoast(
      @PathVariable Long studioBoastId,
      @AuthenticationPrincipal Long musicianId
  );
}
