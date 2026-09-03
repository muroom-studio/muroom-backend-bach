package kr.muroom.muroombackendbach.report.presentation.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.domain.enums.ReportStatus;
import lombok.Builder;

@Builder
public record SearchReportResponse(
    Long reportId,
    ReportDomainType targetType,
    Long targetId,
    ReportReasonDto reason,
    String description,
    ReportStatus status,
    JsonNode snapshot
) {

}

