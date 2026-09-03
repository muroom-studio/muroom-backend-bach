package kr.muroom.muroombackendbach.report.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ReportReasonResponse(
    String id,
    @Schema(example = "FALSE_INFORMATION")
    String code,
    @Schema(example = "허위사실 유포")
    String name
) {

}
