package kr.muroom.muroombackendbach.report.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.report.application.ReportReasonService;
import kr.muroom.muroombackendbach.report.presentation.dto.response.ReportReasonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "신고 유형 API", description = "신고 유형 관련 API")
@RestController
@RequestMapping("/api/v1/report-reason")
@RequiredArgsConstructor
public class ReportReasonController {

  private final ReportReasonService reportReasonService;

  @Operation(
      summary = "신고 유형 전체 조회",
      description = "신고 등록 시 사용되는 모든 신고 유형을 조회합니다."
  )
  @GetMapping
  public ApiResponse<List<ReportReasonResponse>> getAllReportReason() {
    return ApiResponse.success(reportReasonService.getAllReportReason());
  }

}
