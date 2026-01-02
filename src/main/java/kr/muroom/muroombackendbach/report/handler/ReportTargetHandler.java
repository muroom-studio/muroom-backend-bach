package kr.muroom.muroombackendbach.report.handler;

import com.fasterxml.jackson.databind.JsonNode;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;

public interface ReportTargetHandler {

  ReportDomainType supports();

  /**
   * 존재/삭제/권한 등 정책 검증
   */
  void validateTarget(Long domainId, Musician reporter);

  /**
   * 운영용 snapshot JSON 생성 (jsonb 저장용)
   */
  JsonNode buildSnapshot(Long domainId);
}
