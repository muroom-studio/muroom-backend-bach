package kr.muroom.muroombackendbach.faq.presentation.dto.response;

import kr.muroom.muroombackendbach.faq.domain.entity.FaqCategory;

public record FaqCategoryResponse(
    String id,
    String code,
    String name
) {

  public static FaqCategoryResponse from(FaqCategory category) {
    return new FaqCategoryResponse(
        String.valueOf(category.getId()),
        category.getCode(),
        category.getName()
    );
  }
}