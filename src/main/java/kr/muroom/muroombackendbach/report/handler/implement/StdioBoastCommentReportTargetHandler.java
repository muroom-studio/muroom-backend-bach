package kr.muroom.muroombackendbach.report.handler.implement;

import com.fasterxml.jackson.databind.JsonNode;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.report.application.ReportService;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.exception.ReportErrorCode;
import kr.muroom.muroombackendbach.report.handler.ReportTargetHandler;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastComment;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastCommentRepository;
import kr.muroom.muroombackendbach.studioboasting.exception.StudioBoastErrorCode;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StdioBoastCommentReportTargetHandler implements ReportTargetHandler {

  private final StudioBoastCommentRepository studioBoastCommentRepository;

  @Override
  public ReportDomainType supports() {
    return ReportDomainType.STUDIO_BOAST_COMMENT;
  }

  @Override
  public void validateTarget(Long studioBoastCommentId, Musician reporter) {
    StudioBoastComment studioBoastComment = studioBoastCommentRepository.findById(
            studioBoastCommentId)
        .orElseThrow(() -> new BusinessException(
            StudioBoastErrorCode.STUDIO_BOAST_COMMENT_NOT_FOUND));

    if (studioBoastComment.getId().equals(reporter.getId())) {
      throw new BusinessException(ReportErrorCode.REPORT_NOT_ME);
    }
  }

  @Override
  public JsonNode buildSnapshot(Long studioBoastCommentId) {
    StudioBoastComment studioBoastComment = studioBoastCommentRepository.findById(
            studioBoastCommentId)
        .orElseThrow(() -> new BusinessException(
            StudioBoastErrorCode.STUDIO_BOAST_COMMENT_NOT_FOUND));

    return null;
  }
}
