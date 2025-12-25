package kr.muroom.muroombackendbach.report.presentation.dto.request;

public record RegisterReportRequest(
    Long reportReasonId,
    String description
) {

}
