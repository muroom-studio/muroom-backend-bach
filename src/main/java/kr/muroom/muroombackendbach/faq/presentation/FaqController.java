package kr.muroom.muroombackendbach.faq.presentation;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/faqs")
@RequiredArgsConstructor
public class FaqController implements FaqControllerDocs {

  private final FaqService faqService;

  @GetMapping
  public ApiResponse<PaginatedData<FaqResponse>> getFaqs(
      @RequestParam(name = "keyword", required = false) String keyword,
      @RequestParam(name = "categoryId", required = false) Long categoryId,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
  ) {
    return ApiResponse.success(
        PaginatedData.from(faqService.findFaqs(keyword, categoryId, pageable))
    );
  }
}
