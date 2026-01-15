package kr.muroom.muroombackendbach.admin.report.presentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.admin.report.presentation.dto.request.RegisterReportReplyRequest;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.report.application.ReportReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자용 신고 답변 API")
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

  private final ReportReplyService reportReplyService;

  @PostMapping("/{reportId}/reply")
  public ApiResponse<Void> registerReportReply(
      @PathVariable Long reportId,
      @RequestBody @Valid RegisterReportReplyRequest request) {
    reportReplyService.registerReportReply(reportId, request);
    return ApiResponse.success();
  }

  @DeleteMapping("/reply/{reportReplyId}")
  public ApiResponse<Void> deleteReportReply(
      @PathVariable Long reportReplyId
  ) {
    reportReplyService.deleteReportReply(reportReplyId);
    return ApiResponse.success();
  }

}
