package kr.muroom.muroombackendbach.report.presentation;

import io.swagger.v3.oas.annotations.Parameter;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.report.application.ReportService;
import kr.muroom.muroombackendbach.report.presentation.dto.response.ReportsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @GetMapping
  public ApiResponse<PaginatedData<ReportsResponse>> getMyReports(
      @AuthenticationPrincipal Long musicianId,
      @Parameter(hidden = true)
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
  ) {
    return ApiResponse.success(
        PaginatedData.from(reportService.getMyReports(musicianId, pageable)));
  }

  @DeleteMapping("/{reportId}")
  public ApiResponse<Void> deleteMyReport(
      @AuthenticationPrincipal Long musicianId,
      @PathVariable Long reportId
  ) {
    reportService.deleteMyReport(musicianId, reportId);
    return ApiResponse.success();
  }
}
