package kr.muroom.muroombackendbach.inquiry.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryCategoryResponse;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "1:1 문의 카테고리 API", description = "1:1 문의 카테고리 관련 API")
public interface InquiryCategoryControllerDocs {

  @Operation(
      summary = "1:1 문의 카테고리 전체 조회",
      description = "1:1 문의 등록 시 사용되는 모든 문의 카테고리를 조회합니다."
  )
  @GetMapping
  ApiResponse<List<InquiryCategoryResponse>> getAllInquiryCategories();
}
