package kr.muroom.muroombackendbach.terms.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record TermSimpleResponse(
    @Schema(description = "약관 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    String termId,

    @Schema(description = "약관 제목", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(description = "약관 내용", requiredMode = Schema.RequiredMode.REQUIRED)
    String content
) {

}
