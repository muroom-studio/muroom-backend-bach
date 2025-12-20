package kr.muroom.muroombackendbach.faq.presentation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.faq.application.FaqService;
import kr.muroom.muroombackendbach.faq.presentation.dto.response.FaqResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FAQ API", description = "등록된 FAQ 페이지를 가져옵니다.")
@RestController
@RequestMapping("/api/v1/faqs")
@RequiredArgsConstructor
public class FaqController {

  private final FaqService faqService;

  @GetMapping
  public ApiResponse<PaginatedData<FaqResponse>> getAllFaqs(
      @Parameter(hidden = true)
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
  ) {
    return ApiResponse.success(PaginatedData.from(faqService.getAllFaqs(pageable)));
  }
}
