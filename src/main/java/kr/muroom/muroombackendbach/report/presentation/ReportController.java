package kr.muroom.muroombackendbach.report.presentation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.SearchInquiryRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.SearchInquiryResponse;
import kr.muroom.muroombackendbach.report.application.ReportService;
import kr.muroom.muroombackendbach.report.presentation.dto.request.SearchReportRequest;
import kr.muroom.muroombackendbach.report.presentation.dto.response.ReportsResponse;
import kr.muroom.muroombackendbach.report.presentation.dto.response.SearchReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "신고 API", description = "신고 조회 / 삭제")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @PreAuthorize("isAuthenticated()")
  @GetMapping
  public ApiResponse<PaginatedData<ReportsResponse>> getMyReports(
      @AuthenticationPrincipal Long musicianId,
      @Parameter(hidden = true)
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
  ) {
    return ApiResponse.success(
        PaginatedData.from(reportService.getMyReports(musicianId, pageable)));
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/search")
  public ApiResponse<PaginatedData<SearchReportResponse>> searchReport(
      @AuthenticationPrincipal Long musicianId,
      @RequestParam(required = false) String keyword,
      @Parameter(hidden = true)
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
  ) {
    Page<SearchReportResponse> responses = reportService.searchReports(musicianId,
        keyword, pageable);
    return ApiResponse.success(PaginatedData.from(responses));
  }

  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{reportId}")
  public ApiResponse<Void> deleteMyReport(
      @AuthenticationPrincipal Long musicianId,
      @PathVariable Long reportId
  ) {
    reportService.deleteMyReport(musicianId, reportId);
    return ApiResponse.success();
  }
}
