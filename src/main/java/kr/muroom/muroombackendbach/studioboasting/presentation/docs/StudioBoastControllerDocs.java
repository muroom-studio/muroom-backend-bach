package kr.muroom.muroombackendbach.studioboasting.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.CreateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.StudioBoastImageUploadRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.UpdateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastListElementResponse;
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
  @SecurityRequirement(name = "Authentication")
  @PostMapping
  ApiResponse<Long> createStudioBoast(
      @Validated @RequestBody CreateStudioBoastRequest request,
      @AuthenticationPrincipal Long musicianId
  );

  @Operation(
      summary = "작업실 소개(자랑) 게시글 수정",
      description = "본인의 작업실 소개(자랑) 게시글을 수정합니다."
  )
  @SecurityRequirement(name = "Authentication")
  @PutMapping("/{studioBoastId}")
  ApiResponse<Long> updateStudioBoast(
      @PathVariable Long studioBoastId,
      @Validated @RequestBody UpdateStudioBoastRequest request,
      @AuthenticationPrincipal Long musicianId
  );

  @Operation(
      summary = "작업실 소개(자랑) 게시글 상세 조회",
      description = "작업실 소개(자랑) 게시글의 상세 정보를 조회합니다."
  )
  @GetMapping("/{studioBoastId}")
  ApiResponse<StudioBoastDetailResponse> getStudioBoastDetail(@PathVariable Long studioBoastId);

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
  ApiResponse<PaginatedData<StudioBoastListElementResponse>> getStudioBoasts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(name = "sort", defaultValue = "latest,desc") String sort
  );

  @Operation(
      summary = "작업실 소개(자랑) 게시글 삭제",
      description = "본인의 작업실 소개(자랑) 게시글을 삭제합니다."
  )
  @DeleteMapping("/{studioBoastId}")
  @SecurityRequirement(name = "Authentication")
  ApiResponse<Void> deleteStudioBoast(
      @PathVariable Long studioBoastId,
      @AuthenticationPrincipal Long musicianId
  );
}
