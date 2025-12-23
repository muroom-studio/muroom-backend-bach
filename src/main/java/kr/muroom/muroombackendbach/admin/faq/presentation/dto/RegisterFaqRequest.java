package kr.muroom.muroombackendbach.admin.faq.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RegisterFaqRequest(
    Long categoryId,

    @Schema(example = "밥은 어떻게 먹나요?", description = "text 형식")
    @NotBlank
    String question,

    @Schema(example = "잘?", description = "text 형식")
    @NotBlank
    String answer
) {

}
