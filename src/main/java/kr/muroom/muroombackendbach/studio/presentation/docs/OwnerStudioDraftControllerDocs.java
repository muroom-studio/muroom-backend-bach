package kr.muroom.muroombackendbach.studio.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.request.StudioDraftSaveRequest;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDraftDetailResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDraftListResponse;

@Tag(name = "Owner - 스튜디오 임시 저장 API")
@SuppressWarnings("unused")
public interface OwnerStudioDraftControllerDocs {

  @Operation(
      summary = "스튜디오 임시 저장 생성",
      description = "사장님이 스튜디오 등록 중 현재 단계의 데이터를 임시 저장합니다. 임시 저장본은 생성 후 3일간 유효합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "임시 저장 생성 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400", description = "입력값 검증 실패 (step이 null이거나 1~8 범위 초과)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"step: 1 이상이어야 합니다.\"}"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401", description = "인증 실패 (로그인 필요)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 401, \"code\": \"AU-401-01\", \"message\": \"인증이 필요한 서비스입니다.\"}"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403", description = "권한 없음 (OWNER 권한 필요)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 403, \"code\": \"AU-403-01\", \"message\": \"해당 리소스에 접근 권한이 없습니다.\"}")))
  })
  ApiResponse<String> createStudioDraft(
      @Parameter(hidden = true) Long ownerId,
      StudioDraftSaveRequest request
  );

  @Operation(
      summary = "스튜디오 임시 저장 목록 조회",
      description = "로그인한 사장님이 저장한 모든 임시 저장본 목록을 최근 수정 순으로 조회합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401", description = "인증 실패 (로그인 필요)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 401, \"code\": \"AU-401-01\", \"message\": \"인증이 필요한 서비스입니다.\"}"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403", description = "권한 없음 (OWNER 권한 필요)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 403, \"code\": \"AU-403-01\", \"message\": \"해당 리소스에 접근 권한이 없습니다.\"}")))
  })
  ApiResponse<List<StudioDraftListResponse>> getStudioDrafts(
      @Parameter(hidden = true) Long ownerId
  );

  @Operation(
      summary = "스튜디오 임시 저장 단건 조회",
      description = "임시 저장본 ID로 상세 데이터를 조회합니다. 본인 소유의 임시 저장본만 조회 가능합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400", description = "임시 저장본을 찾을 수 없음 (존재하지 않거나 본인 소유가 아님)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 400, \"code\": \"SD-404-71\", \"message\": \"해당 스튜디오 임시 저장본을 찾을 수 없습니다.\"}"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401", description = "인증 실패 (로그인 필요)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 401, \"code\": \"AU-401-01\", \"message\": \"인증이 필요한 서비스입니다.\"}"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403", description = "권한 없음 (OWNER 권한 필요)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 403, \"code\": \"AU-403-01\", \"message\": \"해당 리소스에 접근 권한이 없습니다.\"}")))
  })
  ApiResponse<StudioDraftDetailResponse> getStudioDraft(
      @Parameter(hidden = true) Long ownerId,
      @Parameter(description = "임시 저장본 ID", example = "816559125778992227") Long studioDraftId
  );

  @Operation(
      summary = "스튜디오 임시 저장 수정",
      description = "기존 임시 저장본을 덮어씁니다. 수정 시 유효 기간이 현재 시점으로부터 3일 연장됩니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400", description = "입력값 검증 실패 (step이 null이거나 1~8 범위 초과, 사진 개수 초과 등)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 400, \"message\": \"step: 1 이상이어야 합니다.\"}"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400", description = "임시 저장본을 찾을 수 없음 (존재하지 않거나 본인 소유가 아님)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 400, \"code\": \"SD-404-71\", \"message\": \"해당 스튜디오 임시 저장본을 찾을 수 없습니다.\"}"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401", description = "인증 실패 (로그인 필요)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 401, \"code\": \"AU-401-01\", \"message\": \"인증이 필요한 서비스입니다.\"}"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403", description = "권한 없음 (OWNER 권한 필요)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 403, \"code\": \"AU-403-01\", \"message\": \"해당 리소스에 접근 권한이 없습니다.\"}")))
  })
  ApiResponse<Void> updateStudioDraft(
      @Parameter(hidden = true) Long ownerId,
      @Parameter(description = "임시 저장본 ID", example = "816559125778992227") Long studioDraftId,
      StudioDraftSaveRequest request
  );

  @Operation(
      summary = "스튜디오 임시 저장 삭제",
      description = "임시 저장본을 영구 삭제합니다. 본인 소유의 임시 저장본만 삭제 가능합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401", description = "인증 실패 (로그인 필요)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 401, \"code\": \"AU-401-01\", \"message\": \"인증이 필요한 서비스입니다.\"}"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403", description = "권한 없음 (OWNER 권한 필요)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 403, \"code\": \"AU-403-01\", \"message\": \"해당 리소스에 접근 권한이 없습니다.\"}"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400", description = "임시 저장본을 찾을 수 없음 (존재하지 않거나 본인 소유가 아님)",
          content = @Content(schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  value = "{\"status\": 400, \"code\": \"SD-404-71\", \"message\": \"해당 스튜디오 임시 저장본을 찾을 수 없습니다.\"}")))
  })
  ApiResponse<Void> deleteStudioDraft(
      @Parameter(hidden = true) Long ownerId,
      @Parameter(description = "삭제할 임시 저장본 ID", example = "816559125778992227") Long studioDraftId
  );
}
