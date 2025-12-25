package kr.muroom.muroombackendbach.faq.presentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.faq.application.FaqCategoryService;
import kr.muroom.muroombackendbach.faq.presentation.dto.response.FaqCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FAQ 카테고리 API", description = "faq 카테고리 관련 ")
@RestController
@RequestMapping("/api/v1/faq-categories")
@RequiredArgsConstructor
public class FaqCategoryController {

  public final FaqCategoryService faqCategoryService;

  @GetMapping
  public ApiResponse<List<FaqCategoryResponse>> getAllFaqCategory() {
    return ApiResponse.success(faqCategoryService.getAllFaqCategory());
  }
}
