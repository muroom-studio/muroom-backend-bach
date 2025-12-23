package kr.muroom.muroombackendbach.faq.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.faq.presentation.dto.response.FaqResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "FAQ API", description = "FAQ 목록 조회 및 키워드/카테고리 필터 검색 API")
public interface FaqControllerDocs {

  @Operation(
      summary = "FAQ 목록 조회/검색",
      description = """
          FAQ를 페이지 단위로 조회합니다.
          
          ✅ 동작 규칙
          - keyword, categoryId 모두 없으면: 전체 FAQ 조회
          - keyword만 있으면: 질문(question) + 답변(answer)에서 키워드 검색
          - categoryId만 있으면: 해당 카테고리의 FAQ만 필터링
          - keyword + categoryId 모두 있으면: 카테고리 내에서 키워드 검색
          
          ✅ 예시
          - 전체 조회: /api/v1/faqs
          - 키워드 검색: /api/v1/faqs?keyword=작업실
          - 카테고리 필터: /api/v1/faqs?categoryId=1
          - 복합 검색: /api/v1/faqs?keyword=등록&categoryId=1
          """
  )
  @Parameters({
      @Parameter(
          name = "keyword",
          description = "검색 키워드(질문/답변 대상). 미입력 또는 공백이면 전체 조회로 동작합니다.",
          example = "작업실",
          schema = @Schema(type = "string", nullable = true)
      ),
      @Parameter(
          name = "categoryId",
          description = "FAQ 카테고리 ID. 미입력 시 카테고리 필터 없이 조회합니다.",
          example = "1",
          schema = @Schema(type = "integer", format = "int64", nullable = true)
      )
  })
  @GetMapping
  public ApiResponse<PaginatedData<FaqResponse>> getFaqs(
      @RequestParam(name = "keyword", required = false) String keyword,
      @RequestParam(name = "categoryId", required = false) Long categoryId,
      @Parameter(hidden = true)
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
  );
}
