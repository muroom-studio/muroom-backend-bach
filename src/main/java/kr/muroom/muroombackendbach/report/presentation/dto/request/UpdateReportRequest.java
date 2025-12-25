package kr.muroom.muroombackendbach.report.presentation.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateReportRequest(
    Long reportReasonId,

    @Size(max = 1000, message = "신고 설명은 1000자 이하만 가능합니다.")
    String description
) {

}
