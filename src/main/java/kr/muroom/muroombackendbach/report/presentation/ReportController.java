package kr.muroom.muroombackendbach.report.presentation;

import kr.muroom.muroombackendbach.auth.annotation.CurrentUserId;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.report.application.ReportService;
import kr.muroom.muroombackendbach.report.presentation.docs.ReportControllerDocs;
import kr.muroom.muroombackendbach.report.presentation.dto.request.UpdateReportRequest;
import kr.muroom.muroombackendbach.report.presentation.dto.response.ReportsResponse;
import kr.muroom.muroombackendbach.report.presentation.dto.response.SearchReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController implements ReportControllerDocs {

  private final ReportService reportService;

  @PreAuthorize("hasRole('MUSICIAN')")
  @GetMapping
  public ApiResponse<PaginatedData<ReportsResponse>> getMyReports(
      @CurrentUserId Long musicianId,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
  ) {
    return ApiResponse.success(
        PaginatedData.from(reportService.getMyReports(musicianId, pageable)));
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @GetMapping("/search")
  public ApiResponse<PaginatedData<SearchReportResponse>> searchReport(
      @CurrentUserId Long musicianId,
      @RequestParam(required = false) String keyword,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
  ) {
    Page<SearchReportResponse> responses = reportService.searchReports(musicianId,
        keyword, pageable);
    return ApiResponse.success(PaginatedData.from(responses));
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @PatchMapping("/{reportId}")
  public ApiResponse<Void> updateMyReport(
      @CurrentUserId Long musicianId,
      @PathVariable Long reportId,
      @RequestBody UpdateReportRequest request
  ) {
    reportService.updateMyReport(musicianId, reportId, request);
    return ApiResponse.success();
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @DeleteMapping("/{reportId}")
  public ApiResponse<Void> deleteMyReport(
      @CurrentUserId Long musicianId,
      @PathVariable Long reportId
  ) {
    reportService.deleteMyReport(musicianId, reportId);
    return ApiResponse.success();
  }
}
