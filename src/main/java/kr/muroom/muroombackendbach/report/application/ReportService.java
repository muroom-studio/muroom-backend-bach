package kr.muroom.muroombackendbach.report.application;

import static kr.muroom.muroombackendbach.report.exception.ReportErrorCode.REPORT_FORBIDDEN;
import static kr.muroom.muroombackendbach.report.exception.ReportErrorCode.REPORT_NOT_FOUND;
import static kr.muroom.muroombackendbach.report.exception.ReportErrorCode.REPORT_REASON_NOT_FOUND;

import com.fasterxml.jackson.databind.JsonNode;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.report.domain.entity.Report;
import kr.muroom.muroombackendbach.report.domain.entity.ReportReason;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.domain.enums.ReportStatus;
import kr.muroom.muroombackendbach.report.domain.repository.ReportReasonRepository;
import kr.muroom.muroombackendbach.report.domain.repository.ReportRepository;
import kr.muroom.muroombackendbach.report.exception.ReportErrorCode;
import kr.muroom.muroombackendbach.report.handler.ReportTargetHandler;
import kr.muroom.muroombackendbach.report.handler.ReportTargetHandlerRegistry;
import kr.muroom.muroombackendbach.report.presentation.dto.request.RegisterReportRequest;
import kr.muroom.muroombackendbach.report.presentation.dto.request.UpdateReportRequest;
import kr.muroom.muroombackendbach.report.presentation.dto.response.ReportsResponse;
import kr.muroom.muroombackendbach.report.presentation.dto.response.SearchReportResponse;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import kr.muroom.muroombackendbach.musician.domain.repository.MusicianRepository;
import kr.muroom.muroombackendbach.musician.exception.MusicianErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

  private final ReportRepository reportRepository;
  private final ReportReasonRepository reportReasonRepository;
  private final MusicianRepository musicianRepository;
  private final ReportTargetHandlerRegistry handlerRegistry;

  @Transactional
  public void registerReport(ReportDomainType type, Long domainId, Long musicianId,
      RegisterReportRequest request) {
    // 1. 신고자
    Musician reporter = musicianRepository.findById(musicianId)
        .orElseThrow(() -> new BusinessException(MusicianErrorCode.MUSICIAN_NOT_FOUND));

    // 2. 신고 유형 찾기
    ReportReason reportReason = reportReasonRepository.findById(request.reportReasonId())
        .orElseThrow(() -> new BusinessException(ReportErrorCode.REPORT_REASON_NOT_FOUND));

    // 중복 신고 방지
    boolean exists = reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
        reporter.getId(), type, domainId);
    if (exists) {
      throw new BusinessException(ReportErrorCode.REPORT_ALREADY_EXISTS);
    }

    ReportTargetHandler handler = handlerRegistry.get(type);

    // 도메인별 검증
    handler.validateTarget(domainId, reporter);

    // 운영용 snapshot(JSONB)
    JsonNode snapshotJson = handler.buildSnapshot(domainId);

    Report report = Report.builder()
        .reporter(reporter)
        .targetType(type)
        .targetId(domainId)
        .reportReason(reportReason)
        .description(request.description())
        .status(ReportStatus.SUBMITTED)
        .snapshot(snapshotJson)
        .build();

    reportRepository.save(report);
  }

  public Page<ReportsResponse> getMyReports(Long musicianId, Pageable pageable) {
    Page<Report> reports = reportRepository.findByReporterId(musicianId,
        pageable);

    return reports.map(this::toListItem);
  }

  @Transactional
  public void deleteMyReport(Long musicianId, Long reportId) {
    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new BusinessException(REPORT_NOT_FOUND));

    if (!report.getReporter().getId().equals(musicianId)) {
      throw new BusinessException(REPORT_FORBIDDEN);
    }

    reportRepository.delete(report);
  }

  private ReportsResponse toListItem(Report report) {
    return new ReportsResponse(
        report.getId(),
        report.getTargetType(),
        report.getTargetId(),
        new ReportsResponse.Reason(
            report.getReportReason().getId(),
            report.getReportReason().getCode(),
            report.getReportReason().getDescription()
        ),
        report.getDescription(),
        report.getStatus(),
        report.getSnapshot()
    );
  }

  public Page<SearchReportResponse> searchReports(Long musicianId,
      String keyword, Pageable pageable) {
    if (keyword == null || keyword.isBlank()) {
      keyword = "";
    }

    Page<Report> reports = reportRepository.searchByDescription(musicianId, keyword, pageable);

    return reports.map(report -> SearchReportResponse.builder()
        .reportId(report.getId())
        .targetType(report.getTargetType())
        .targetId(report.getTargetId())
        .status(report.getStatus())
        .reason(new SearchReportResponse.Reason(
            report.getReportReason().getId(),
            report.getReportReason().getCode(),
            report.getReportReason().getDescription()
        ))
        .description(report.getDescription())
        .snapshot(report.getSnapshot())
        .build()
    );
  }

  @Transactional
  public void updateMyReport(Long musicianId, Long reportId, UpdateReportRequest request) {
    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new BusinessException(REPORT_NOT_FOUND));

    if (!report.getReporter().getId().equals(musicianId)) {
      throw new BusinessException(REPORT_FORBIDDEN);
    }

    if (report.getStatus() != ReportStatus.SUBMITTED) {
      throw new BusinessException(ReportErrorCode.REPORT_NOT_EDITABLE);
    }

    boolean updated = false;

    if (request.reportReasonId() != null) {
      ReportReason newReason = reportReasonRepository.findById(request.reportReasonId())
          .orElseThrow(() -> new BusinessException(REPORT_REASON_NOT_FOUND));

      report.changeReportReason(newReason);
      updated = true;
    }

    if (request.description() != null) {
      report.changeDescription(request.description().trim());
      updated = true;
    }

    if (!updated) {
      throw new BusinessException(ReportErrorCode.REPORT_INVALID_REQUEST);
    }
  }
}