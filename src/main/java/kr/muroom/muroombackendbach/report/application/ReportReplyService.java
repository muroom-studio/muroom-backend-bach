package kr.muroom.muroombackendbach.report.application;

import kr.muroom.muroombackendbach.admin.report.presentation.dto.request.RegisterReportReplyRequest;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.report.domain.entity.Report;
import kr.muroom.muroombackendbach.report.domain.entity.ReportReply;
import kr.muroom.muroombackendbach.report.domain.repository.ReportReplyRepository;
import kr.muroom.muroombackendbach.report.domain.repository.ReportRepository;
import kr.muroom.muroombackendbach.report.exception.ReportErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class ReportReplyService {

  private final ReportReplyRepository reportReplyRepository;
  private final ReportRepository reportRepository;

  public void registerReportReply(Long reportId, RegisterReportReplyRequest request) {
    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new BusinessException(ReportErrorCode.REPORT_NOT_FOUND));

    // 처리 상태 변경
    report.changeStatus(request.reportStatus());

    ReportReply reply = ReportReply.builder()
        .report(report)
        .message(request.message())
        .build();

    reportReplyRepository.save(reply);
  }

  public void deleteReportReply(Long reportReplyId) {
    reportReplyRepository.deleteById(reportReplyId);
  }
}
