package kr.muroom.muroombackendbach.report.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterReportRequest(
    Long reportReasonId,
    @Schema(example = "기분이 좋지 않아서 신고합니다!")
    String description
) {

}
