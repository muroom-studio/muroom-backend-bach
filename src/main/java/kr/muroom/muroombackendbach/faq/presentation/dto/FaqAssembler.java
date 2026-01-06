package kr.muroom.muroombackendbach.faq.presentation.dto;

import kr.muroom.muroombackendbach.admin.faq.presentation.dto.RegisterFaqRequest;
import kr.muroom.muroombackendbach.faq.domain.entity.Faq;
import kr.muroom.muroombackendbach.faq.domain.entity.FaqCategory;
import kr.muroom.muroombackendbach.faq.presentation.dto.response.FaqResponse;
import org.springframework.stereotype.Component;

@Component
public class FaqAssembler {

  public Faq toEntity(RegisterFaqRequest request, FaqCategory category) {
    return Faq.builder()
        .question(request.question())
        .answer(request.answer())
        .category(category)
        .build();
  }

  public FaqResponse toResponse(Faq faq) {
    if (faq == null) {
      return null;
    }

    return FaqResponse.builder()
        .faqId(String.valueOf(faq.getId()))
        .category(toCategoryResponse(faq.getCategory()))
        .question(faq.getQuestion())
        .answer(faq.getAnswer())
        .build();
  }

  private FaqResponse.Category toCategoryResponse(FaqCategory category) {
    if (category == null) {
      return null;
    }

    return FaqResponse.Category.builder()
        .categoryId(String.valueOf(category.getId()))
        .code(category.getCode())
        .name(category.getName())
        .build();
  }
}
