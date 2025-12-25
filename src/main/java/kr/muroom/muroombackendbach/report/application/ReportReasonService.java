package kr.muroom.muroombackendbach.report.application;

import java.util.List;
import kr.muroom.muroombackendbach.report.domain.repository.ReportReasonRepository;
import kr.muroom.muroombackendbach.report.presentation.dto.response.ReportReasonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportReasonService {

  private final ReportReasonRepository reportReasonRepository;

  public List<ReportReasonResponse> getAllReportReason() {
    return reportReasonRepository.findAll()
        .stream()
        .map(reason -> ReportReasonResponse.builder()
            .id(String.valueOf(reason.getId()))
            .code(reason.getCode())
            .name(reason.getDescription())
            .build())
        .toList();
  }
}
