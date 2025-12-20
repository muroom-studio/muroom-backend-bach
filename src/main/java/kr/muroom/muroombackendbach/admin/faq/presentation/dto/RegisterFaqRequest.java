package kr.muroom.muroombackendbach.admin.faq.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterFaqRequest(
    Long categoryId,

    @Schema(example = "밥은 어떻게 먹나요?", description = "text 형식")
    String question,

    @Schema(example = "잘?", description = "text 형식")
    String answer
) {

}
