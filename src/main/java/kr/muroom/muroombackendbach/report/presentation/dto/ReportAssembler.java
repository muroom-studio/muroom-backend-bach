package kr.muroom.muroombackendbach.report.presentation.dto;

import com.fasterxml.jackson.databind.JsonNode;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import kr.muroom.muroombackendbach.report.domain.entity.Report;
import kr.muroom.muroombackendbach.report.domain.entity.ReportReason;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.domain.enums.ReportStatus;
import kr.muroom.muroombackendbach.report.presentation.dto.request.RegisterReportRequest;
import kr.muroom.muroombackendbach.report.presentation.dto.response.ReportsResponse;
import kr.muroom.muroombackendbach.report.presentation.dto.response.SearchReportResponse;
import org.springframework.stereotype.Component;

@Component
public class ReportAssembler {

  /**
   * 신고 등록용 Report 엔티티 생성 - 생성 전 검증/조회는 Service가 담당
   */
  public Report createReportForRegister(
      Musician reporter,
      ReportDomainType type,
      Long domainId,
      ReportReason reportReason,
      RegisterReportRequest request,
      JsonNode snapshotJson
  ) {
    return Report.builder()
        .reporter(reporter)
        .targetType(type)
        .targetId(domainId)
        .reportReason(reportReason)
        .description(request.description())
        .status(ReportStatus.SUBMITTED)
        .snapshot(snapshotJson)
        .build();
  }

  /**
   * 내 신고 목록 아이템 응답 변환 (Report -> ReportsResponse)
   */
  public ReportsResponse toReportsResponse(Report report) {
    return new ReportsResponse(
        report.getId(),
        report.getTargetType(),
        report.getTargetId(),
        toReportsReason(report.getReportReason()),
        report.getDescription(),
        report.getStatus(),
        report.getSnapshot()
    );
  }

  private ReportsResponse.Reason toReportsReason(ReportReason reason) {
    return new ReportsResponse.Reason(
        reason.getId(),
        reason.getCode(),
        reason.getDescription()
    );
  }

  /**
   * 신고 검색 응답 변환 (Report -> SearchReportResponse)
   */
  public SearchReportResponse toSearchReportResponse(Report report) {
    return SearchReportResponse.builder()
        .reportId(report.getId())
        .targetType(report.getTargetType())
        .targetId(report.getTargetId())
        .status(report.getStatus())
        .reason(toSearchReason(report.getReportReason()))
        .description(report.getDescription())
        .snapshot(report.getSnapshot())
        .build();
  }

  private SearchReportResponse.Reason toSearchReason(ReportReason reason) {
    return new SearchReportResponse.Reason(
        reason.getId(),
        reason.getCode(),
        reason.getDescription()
    );
  }
}
