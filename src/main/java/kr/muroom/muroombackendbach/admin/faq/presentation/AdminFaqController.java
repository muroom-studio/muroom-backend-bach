package kr.muroom.muroombackendbach.admin.faq.presentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.admin.faq.presentation.dto.RegisterFaqRequest;
import kr.muroom.muroombackendbach.admin.faq.presentation.dto.UpdateFaqRequest;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.faq.application.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자용 FAQ API")
@RestController
@RequestMapping("/api/admin/faqs")
@RequiredArgsConstructor
public class AdminFaqController {

  private final FaqService faqService;

  @PostMapping
  public ApiResponse<Void> registerFaq(@RequestBody RegisterFaqRequest request) {
    faqService.registerFaq(request);
    return ApiResponse.success();
  }

  @PutMapping("/{faqId}")
  public ApiResponse<Void> updateFaq(
      @PathVariable Long faqId,
      @RequestBody UpdateFaqRequest request
  ) {
    faqService.updateFaq(faqId, request);
    return ApiResponse.success();
  }

  @DeleteMapping("/{faqId}")
  public ApiResponse<Void> deleteFaq(@PathVariable Long faqId) {
    faqService.deleteFaq(faqId);
    return ApiResponse.success();
  }

}
