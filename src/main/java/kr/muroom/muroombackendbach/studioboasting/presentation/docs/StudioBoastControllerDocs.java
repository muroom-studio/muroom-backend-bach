package kr.muroom.muroombackendbach.studioboasting.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.CreateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.StudioBoastImageUploadRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.UpdateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "~/studio-boasts - 작업실 소개(자랑) API", description = "내 작업실 소개(자랑) 및 해당 컨텐츠 댓글, 좋아요 관련 API")
public interface StudioBoastControllerDocs {

  @Operation(
      summary = "작업실 소개(자랑) 이미지 업로드용 Presigned URL 생성",
      description = "작업실 소개(자랑) 이미지 업로드를 위한 Presigned URL을 생성합니다."
  )
  @SecurityRequirement(name = "Authentication")
  @PostMapping("/presigned-url")
  ApiResponse<GeneratePresignedPutUrlResponse> generateStudioBoastImagePresignedUrls(
      @Validated @RequestBody StudioBoastImageUploadRequest request
  );

  @Operation(
      summary = "작업실 소개(자랑) 게시글 생성",
      description = "새로운 작업실 소개(자랑) 게시글을 생성합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "작업실 연결을 요청했는데, 해당 작업실이 존재하지 않는 경우",
          content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BusinessException.class),
                  examples = {
                      @ExampleObject(
                          name = "작업실 소개(자랑) 게시글 없음",
                          value = """
                              {
                                  "status": 404,
                                  "code": "ST-404-01",
                                  "message": "해당 스튜디오를 찾을 수 없습니다.",
                              }
                              """
                      )
                  }
              )
          }
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "이벤트 약관에 동의하지 않은 경우",
          content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BusinessException.class),
                  examples = {
                      @ExampleObject(
                          name = "이벤트 약관 미동의",
                          value = """
                              {
                                  "status": 400,
                                  "code": "SB-400-91",
                                  "message": "인스타그램 계정 입력 시 이벤트 약관에 동의해야 합니다.",
                              }
                              """
                      )
                  }
              )
          }
      )
  })
  @SecurityRequirement(name = "Authentication")
  @PostMapping
  ApiResponse<String> createStudioBoast(
      @Validated @RequestBody CreateStudioBoastRequest request,
      @AuthenticationPrincipal Long musicianId
  );

  @Operation(
      summary = "작업실 소개(자랑) 게시글 수정",
      description = "본인의 작업실 소개(자랑) 게시글을 수정합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "작업실 연결을 요청했는데, 해당 작업실이 존재하지 않는 경우",
          content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BusinessException.class),
                  examples = {
                      @ExampleObject(
                          name = "작업실 소개(자랑) 게시글 없음",
                          value = """
                              {
                                  "status": 404,
                                  "code": "ST-404-01",
                                  "message": "해당 스튜디오를 찾을 수 없습니다.",
                              }
                              """
                      )
                  }
              )
          }
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
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
                                  "status": 404,
                                  "code": "SB-404-01",
                                  "message": "해당 작업실 소개(자랑)글을 찾을 수 없습니다.",
                              }
                              """
                      )
                  }
              )
          }
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "이벤트 약관에 동의하지 않은 경우",
          content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BusinessException.class),
                  examples = {
                      @ExampleObject(
                          name = "이벤트 약관 미동의",
                          value = """
                              {
                                  "status": 400,
                                  "code": "SB-400-91",
                                  "message": "인스타그램 계정 입력 시 이벤트 약관에 동의해야 합니다.",
                              }
                              """
                      )
                  }
              )
          }
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "본인의 작업실 소개(자랑) 게시글이 아닌 경우",
          content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BusinessException.class),
                  examples = {
                      @ExampleObject(
                          name = "작업실 소개(자랑) 게시글 권한 없음",
                          value = """
                              {
                                  "status": 403,
                                  "code": "AU-403-01",
                                  "message": "해당 리소스에 접근 권한이 없습니다.",
                              }
                              """
                      )
                  }
              )
          }
      )
  })
  @SecurityRequirement(name = "Authentication")
  @PutMapping("/{studioBoastId}")
  ApiResponse<String> updateStudioBoast(
      @PathVariable Long studioBoastId,
      @Validated @RequestBody UpdateStudioBoastRequest request,
      @AuthenticationPrincipal Long musicianId
  );

  @Operation(
      summary = "작업실 소개(자랑) 게시글 상세 조회",
      description = "작업실 소개(자랑) 게시글의 상세 정보를 조회합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
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
                                  "status": 404,
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
  @GetMapping("/{studioBoastId}")
  ApiResponse<StudioBoastDetailResponse> getStudioBoastDetail(
      @PathVariable Long studioBoastId, @AuthenticationPrincipal Long musicianId
  );

  @Operation(summary = "작업실 소개(자랑) 게시글 목록 페이지네이션 조회",
      description = "작업실 소개(자랑) 게시글 목록을 페이지네이션하여 조회합니다. 기본 정렬은 최신순입니다.",
      parameters = {
          @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
          @Parameter(name = "size", description = "페이지 당 항목 수", example = "12"),
          @Parameter(name = "sort", description = "정렬 기준 (예: 'likes,desc', 'latest,desc'). 생략 시 "
              + "기본값은 'latest,desc' (최신순) 입니다.",
              example = "likes,desc")
      }
  )
  @GetMapping
  ApiResponse<PaginatedData<StudioBoastDetailResponse>> getStudioBoasts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(name = "sort", defaultValue = "latest,desc") String sort,
      @AuthenticationPrincipal Long musicianId
  );

  @Operation(summary = "내가 작성한 작업실 소개(자랑) 게시글 목록 페이지네이션 조회",
      description = "내가 작성한 작업실 소개(자랑) 게시글 목록을 페이지네이션하여 조회합니다. 기본 정렬은 최신순입니다.",
      parameters = {
          @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
          @Parameter(name = "size", description = "페이지 당 항목 수", example = "12"),
          @Parameter(name = "sort", description = "정렬 기준 (예: 'likes,desc', 'latest,desc'). 생략 시 "
              + "기본값은 'latest,desc' (최신순) 입니다.",
              example = "likes,desc")
      }
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "인증되지 않은 사용자가 접근하는 경우",
          content = {
              @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject(
                          name = "작업실 소개(자랑) 게시글 권한 없음",
                          value = """
                              {
                                  "timestamp": "2025-12-25T16:53:50.176+00:00",
                                  "status": 401,
                                  "error": "Unauthorized",
                                  "path": "/api/v1/studio-boasts/my"
                              }
                              """
                      )
                  }
              )
          }
      )
  })
  @SecurityRequirement(name = "Authentication")
  @GetMapping("/my")
  ApiResponse<PaginatedData<StudioBoastDetailResponse>> getMyStudioBoasts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(name = "sort", defaultValue = "latest,desc") String sort,
      @AuthenticationPrincipal Long musicianId
  );

  @Operation(
      summary = "작업실 소개(자랑) 게시글 삭제",
      description = "본인의 작업실 소개(자랑) 게시글을 삭제합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
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
                                  "status": 404,
                                  "code": "SB-404-01",
                                  "message": "해당 작업실 소개(자랑)글을 찾을 수 없습니다.",
                              }
                              """
                      )
                  }
              )
          }
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "본인의 작업실 소개(자랑) 게시글이 아닌 경우",
          content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BusinessException.class),
                  examples = {
                      @ExampleObject(
                          name = "작업실 소개(자랑) 게시글 권한 없음",
                          value = """
                              {
                                  "status": 403,
                                  "code": "AU-403-01",
                                  "message": "해당 리소스에 접근 권한이 없습니다.",
                              }
                              """
                      )
                  }
              )
          }
      )
  })
  @DeleteMapping("/{studioBoastId}")
  @SecurityRequirement(name = "Authentication")
  ApiResponse<Void> deleteStudioBoast(
      @PathVariable Long studioBoastId,
      @AuthenticationPrincipal Long musicianId
  );
}
