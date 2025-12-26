package kr.muroom.muroombackendbach.report.handler.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.handler.ReportTargetHandler;
import kr.muroom.muroombackendbach.studioboasting.application.StudioBoastService;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StudioBoastReportTargetHandler implements ReportTargetHandler {

  private final StudioBoastService studioBoastService;
  private final ObjectMapper objectMapper;

  @Override
  public ReportDomainType supports() {
    return ReportDomainType.STUDIO_BOAST;
  }

  @Override
  public void validateTarget(Long domainId, Musician reporter) {

  }

  @Override
  public JsonNode buildSnapshot(Long domainId) {
    return null;
  }
}
