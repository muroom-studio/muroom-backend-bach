package kr.muroom.muroombackendbach.faq.presentation.dto.response;

import kr.muroom.muroombackendbach.faq.domain.entity.FaqCategory;

public record FaqCategoryResponse(
    Long id,
    String code,
    String name
) {

  public static FaqCategoryResponse from(FaqCategory category) {
    return new FaqCategoryResponse(
        category.getId(),
        category.getCode(),
        category.getName()
    );
  }
}