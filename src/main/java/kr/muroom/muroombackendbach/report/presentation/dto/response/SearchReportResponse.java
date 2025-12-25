package kr.muroom.muroombackendbach.report.presentation.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.domain.enums.ReportStatus;
import kr.muroom.muroombackendbach.report.presentation.dto.response.ReportsResponse.Reason;
import lombok.Builder;

@Builder
public record SearchReportResponse(
    Long reportId,
    ReportDomainType targetType,
    Long targetId,
    ReportsResponse.Reason reason,
    String description,
    ReportStatus status,
    JsonNode snapshot
) {

  public record Reason(
      Long id,
      String code,
      String description
  ) {

  }
}

