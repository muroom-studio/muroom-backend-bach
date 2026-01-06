package kr.muroom.muroombackendbach.faq.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.muroom.muroombackendbach.faq.domain.entity.Faq;
import kr.muroom.muroombackendbach.faq.domain.entity.FaqCategory;
import lombok.Builder;

@Builder
public record FaqResponse(
    String faqId,
    Category category,
    @Schema(example = "푸시알림은 어떻게 설정하나요?")
    String question,
    @Schema(example = "저도 몰라요 ^^")
    String answer
) {

  @Builder
  public record Category(
      @Schema(example = "791543436721219205")
      String categoryId,
      @Schema(example = "ACCOUNT", description = "고유한 코드 값")
      String code,
      @Schema(example = "계정")
      String name
  ) {

  }
}
