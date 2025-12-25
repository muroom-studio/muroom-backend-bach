package kr.muroom.muroombackendbach.admin.report.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.muroom.muroombackendbach.report.domain.enums.ReportStatus;

public record RegisterReportReplyRequest(
    @Schema(example = "RESOLVED")
    @NotNull
    ReportStatus reportStatus,

    String message
) {

}
