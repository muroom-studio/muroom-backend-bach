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
import kr.muroom.muroombackendbach.report.presentation.dto.request.RegisterReportRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.CreateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.StudioBoastImageUploadRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.UpdateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastSimpleResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "studio boast - 작업실 소개(자랑) API")
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
      description = "작업실 소개(자랑) 게시글의 상세 정보를 담는 목록을 페이지네이션하여 조회합니다. 기본 정렬은 최신순입니다.",
      parameters = {
          @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
          @Parameter(name = "size", description = "페이지 당 항목 수. 생략 시 기본값은 2입니다.", example = "2"),
          @Parameter(name = "sort", description = "정렬 기준 (예: 'likes,desc', 'latest,desc', 'random'). 생략 시 "
              + "기본값은 'latest,desc' (최신순) 입니다.",
              example = "likes,desc")
      }
  )
  @GetMapping
  ApiResponse<PaginatedData<StudioBoastDetailResponse>> getDetailedStudioBoasts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "2") int size,
      @RequestParam(name = "sort", defaultValue = "latest,desc") String sort,
      @AuthenticationPrincipal Long musicianId
  );

  @Operation(summary = "작업실 자랑 간단 정보 목록 페이지네이션 조회",
      description = "작업실 자랑 게시글의 간단한 정보 목록을 페이지네이션하여 조회합니다. 기본 정렬은 최신순입니다.",
      parameters = {
          @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
          @Parameter(name = "size", description = "페이지 당 항목 수. 생략 시 기본값은 10입니다.", example = "10"),
          @Parameter(name = "sort", description = "정렬 기준 ['likes,desc', 'latest,desc']. 생략 시 "
              + "기본값은 'latest,desc' (최신순) 입니다.",
              example = "latest,desc")
      }
  )
  @GetMapping("/simple")
  ApiResponse<PaginatedData<StudioBoastSimpleResponse>> getSimpleStudioBoasts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(name = "sort", defaultValue = "latest,desc") String sort
  );

  @Operation(summary = "내가 작성한 작업실 소개(자랑) 게시글 목록 페이지네이션 조회",
      description = "내가 작성한 작업실 소개(자랑) 게시글 목록을 페이지네이션하여 조회합니다. 기본 정렬은 최신순입니다.",
      parameters = {
          @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
          @Parameter(name = "size", description = "페이지 당 항목 수. 생략 시 기본값은 2입니다.", example = "2"),
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
  ApiResponse<PaginatedData<StudioBoastDetailResponse>> getMyDetailedStudioBoasts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "2") int size,
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

  @Operation(
      summary = "스튜디오 매물 자랑 신고하기",
      description = """
          스튜디오 매물 자랑 게시글을 신고합니다.
          
          - 이미 신고한 게시글은 다시 신고할 수 없습니다.
          - 본인이 작성한 게시글은 신고할 수 없습니다.
          - 신고 시점의 게시글 정보는 스냅샷(JSON)으로 저장됩니다.
          """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "신고 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = {
                  @ExampleObject(
                      name = "신고 대상 없음",
                      value = """
                          {
                              "status": 400,
                              "code": "RP-400-01",
                              "message": "해당 신고내역을 찾을 수 없습니다."
                          }
                          """
                  ),
                  @ExampleObject(
                      name = "신고 사유 없음",
                      value = """
                          {
                              "status": 400,
                              "code": "RP-400-02",
                              "message": "해당 신고이유 유형을 찾을 수 없습니다."
                          }
                          """
                  ),
                  @ExampleObject(
                      name = "이미 신고한 게시글",
                      value = """
                          {
                              "status": 400,
                              "code": "RP-400-04",
                              "message": "이미 신고 처리 되었습니다."
                          }
                          """
                  ),
                  @ExampleObject(
                      name = "자기 자신 신고",
                      value = """
                          {
                              "status": 400,
                              "code": "RP-400-09",
                              "message": "나를 신고할 수 없습니다."
                          }
                          """
                  )
              }
          )
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "권한 없음",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = {
                  @ExampleObject(
                      name = "권한 없음",
                      value = """
                          {
                              "status": 403,
                              "code": "RP-403-06",
                              "message": "권한이 없습니다."
                          }
                          """
                  )
              }
          )
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "500",
          description = "서버 오류",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = {
                  @ExampleObject(
                      name = "스냅샷 JSON 변환 실패",
                      value = """
                          {
                              "status": 500,
                              "code": "RP-500-05",
                              "message": "JSON 변환 실패했습니다."
                          }
                          """
                  )
              }
          )
      )
  })
  @PostMapping("/{studioBoastId}/report")
  @SecurityRequirement(name = "Authentication")
  ApiResponse<Void> reportStudioBoast(
      @PathVariable Long studioBoastId,
      @AuthenticationPrincipal Long musicianId,
      @RequestBody RegisterReportRequest request
  );

  @Operation(
      summary = "작업실 소개(자랑) 게시글 좋아요 추가",
      description = "작업실 소개(자랑) 게시글에 좋아요를 추가합니다."
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
  @SecurityRequirement(name = "Authentication")
  @DeleteMapping("/{studioBoastId}/likes")
  ApiResponse<Void> unlikeStudioBoast(
      @PathVariable Long studioBoastId,
      @AuthenticationPrincipal Long musicianId
  );
}
