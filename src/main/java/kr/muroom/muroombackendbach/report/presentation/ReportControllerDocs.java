package kr.muroom.muroombackendbach.report.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.report.presentation.dto.request.UpdateReportRequest;
import kr.muroom.muroombackendbach.report.presentation.dto.response.ReportsResponse;
import kr.muroom.muroombackendbach.report.presentation.dto.response.SearchReportResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "report - 신고 API")
public interface ReportControllerDocs {

  @Operation(
      summary = "나의 신고 목록 조회",
      description = """
          로그인한 사용자가 등록한 신고 목록을 페이지 형태로 조회합니다.
          - 기본 정렬: createdAt DESC (구현체에서 @PageableDefault로 적용)
          - 데이터가 없으면 빈 리스트로 응답합니다.
          """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "인증 실패",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "인증 필요",
                      value = """
                          {
                            "code": "AUTH-401-01",
                            "message": "인증이 필요합니다."
                          }
                          """,
                      description = "로그인이 필요하거나 인증 정보가 유효하지 않은 경우"
                  )
              }
          )
      )
  })
  ApiResponse<PaginatedData<ReportsResponse>> getMyReports(
      @Parameter(hidden = true) Long musicianId,
      @Parameter(hidden = true) Pageable pageable
  );

  @Operation(
      summary = "나의 신고 검색",
      description = """
          로그인한 사용자가 등록한 신고를 키워드로 검색합니다.
          - 검색 대상: description(신고자 서술)만 검색
          - keyword가 비어있으면 전체 조회와 동일하게 동작합니다.
          - 기본 정렬: createdAt DESC (구현체에서 @PageableDefault로 적용)
          """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "검색 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "인증 실패",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "인증 필요",
                      value = """
                          {
                            "code": "AUTH-401-01",
                            "message": "인증이 필요합니다."
                          }
                          """,
                      description = "로그인이 필요하거나 인증 정보가 유효하지 않은 경우"
                  )
              }
          )
      )
  })
  ApiResponse<PaginatedData<SearchReportResponse>> searchReport(
      @Parameter(hidden = true) Long musicianId,
      @Parameter(description = "검색 키워드(신고자 서술(description)에서 부분 일치 검색)")
      @RequestParam(required = false) String keyword,
      @Parameter(hidden = true) Pageable pageable
  );

  @Operation(
      summary = "나의 신고 삭제",
      description = """
          로그인한 사용자가 본인이 등록한 신고를 삭제합니다.
          - 신고 내역이 없으면 400(RP-400-01)
          - 본인 신고가 아니면 403(RP-403-06)
          """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "삭제 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "인증 실패",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "인증 필요",
                      value = """
                          {
                            "code": "AUTH-401-01",
                            "message": "인증이 필요합니다."
                          }
                          """,
                      description = "로그인이 필요하거나 인증 정보가 유효하지 않은 경우"
                  )
              }
          )
      ),

      /* ✅ 400: ReportErrorCode의 BAD_REQUEST들을 한 번에 묶기 */
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "신고 내역 없음",
                      value = """
                          {
                            "code": "RP-400-01",
                            "message": "해당 신고내역을 찾을 수 없습니다."
                          }
                          """,
                      description = "reportId에 해당하는 신고가 존재하지 않는 경우"
                  ),
                  @ExampleObject(
                      name = "신고 사유 없음",
                      value = """
                          {
                            "code": "RP-400-02",
                            "message": "해당 신고이유 유형을 찾을 수 없습니다."
                          }
                          """,
                      description = "reportReasonId에 해당하는 신고 사유가 존재하지 않는 경우"
                  ),
                  @ExampleObject(
                      name = "신고 불가 도메인",
                      value = """
                          {
                            "code": "RP-400-03",
                            "message": "해당 도메인을 신고할 수 없습니다."
                          }
                          """,
                      description = "요청한 ReportDomainType이 지원되지 않는 경우"
                  ),
                  @ExampleObject(
                      name = "이미 신고됨",
                      value = """
                          {
                            "code": "RP-400-04",
                            "message": "이미 신고 처리 되었습니다."
                          }
                          """,
                      description = "동일 신고자/대상에 대해 이미 신고가 존재하는 경우"
                  )
              }
          )
      ),

      /* ✅ 403: FORBIDDEN 한 번에 */
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "권한 없음",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "권한 없음",
                      value = """
                          {
                            "code": "RP-403-06",
                            "message": "권한이 없습니다."
                          }
                          """,
                      description = "삭제하려는 신고가 본인 신고가 아닌 경우"
                  )
              }
          )
      ),

      /* ✅ 500: INTERNAL_SERVER_ERROR 한 번에 */
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "500",
          description = "서버 오류",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "스냅샷 JSON 변환 실패",
                      value = """
                          {
                            "code": "RP-500-05",
                            "message": "JSON 변환 실패했습니다."
                          }
                          """,
                      description = "snapshot 생성 과정에서 JSON 직렬화 실패한 경우"
                  )
              }
          )
      )
  })
  ApiResponse<Void> deleteMyReport(
      @Parameter(hidden = true) Long musicianId,
      @Parameter(description = "삭제할 신고 ID", example = "123") Long reportId
  );

  @Operation(
      summary = "나의 신고 수정",
      description = """
          로그인한 사용자가 본인이 등록한 신고 내용을 부분 수정합니다.
          
          - PATCH 방식으로 동작합니다.
          - null로 전달된 필드는 수정되지 않습니다.
          - 수정 가능 항목:
            - reportReasonId (신고 사유 변경)
            - description (신고 설명 수정)
          - 처리 중이거나 완료된 신고는 수정할 수 없습니다.
          """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "수정 성공"
      ),

      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "신고 내역 없음",
                      value = """
                          {
                            "code": "RP-400-01",
                            "message": "해당 신고내역을 찾을 수 없습니다."
                          }
                          """,
                      description = "reportId에 해당하는 신고가 존재하지 않는 경우"
                  ),
                  @ExampleObject(
                      name = "신고 사유 없음",
                      value = """
                          {
                            "code": "RP-400-02",
                            "message": "해당 신고이유 유형을 찾을 수 없습니다."
                          }
                          """,
                      description = "reportReasonId에 해당하는 신고 사유가 존재하지 않는 경우"
                  ),
                  @ExampleObject(
                      name = "수정할 내용 없음",
                      value = """
                          {
                            "code": "RP-400-08",
                            "message": "수정할 내용이 없습니다."
                          }
                          """,
                      description = "요청 본문에 수정 가능한 필드가 하나도 없는 경우"
                  ),
                  @ExampleObject(
                      name = "수정 불가 상태",
                      value = """
                          {
                            "code": "RP-400-07",
                            "message": "처리 중이거나 완료된 신고는 수정할 수 없습니다."
                          }
                          """,
                      description = "신고 상태가 SUBMITTED가 아닌 경우"
                  )
              }
          )
      ),

      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "권한 없음",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "본인 신고 아님",
                      value = """
                          {
                            "code": "RP-403-06",
                            "message": "권한이 없습니다."
                          }
                          """,
                      description = "본인이 등록한 신고가 아닌 경우"
                  )
              }
          )
      ),

      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "인증 실패",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "인증 필요",
                      value = """
                          {
                            "code": "AUTH-401-01",
                            "message": "인증이 필요합니다."
                          }
                          """,
                      description = "로그인이 필요하거나 인증 정보가 유효하지 않은 경우"
                  )
              }
          )
      )
  })
  @PatchMapping("/{reportId}")
  ApiResponse<Void> updateMyReport(
      @Parameter(hidden = true)
      @AuthenticationPrincipal Long musicianId,

      @Parameter(description = "수정할 신고 ID", example = "123")
      @PathVariable Long reportId,

      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = """
              수정할 신고 정보 (PATCH)
              
              - reportReasonId: 변경할 신고 사유 ID (선택)
              - description: 변경할 신고 설명 (선택)
              - null로 전달된 필드는 변경되지 않습니다.
              """,
          required = true,
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "설명만 수정",
                      value = """
                          {
                            "description": "추가로 확인된 문제 내용입니다."
                          }
                          """
                  ),
                  @ExampleObject(
                      name = "신고 사유 + 설명 수정",
                      value = """
                          {
                            "reportReasonId": 3,
                            "description": "허위 정보로 판단됩니다."
                          }
                          """
                  )
              }
          )
      )
      UpdateReportRequest request
  );

}
