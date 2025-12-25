package kr.muroom.muroombackendbach.report.handler.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.exception.ReportErrorCode;
import kr.muroom.muroombackendbach.report.handler.ReportTargetHandler;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioImage;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioRepository;
import kr.muroom.muroombackendbach.studio.exception.StudioErrorCode;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StudioReportTargetHandler implements ReportTargetHandler {

  private final StudioRepository studioRepository;
  private final ObjectMapper objectMapper;

  @Override
  public ReportDomainType supports() {
    return ReportDomainType.STUDIO;
  }

  @Override
  public void validateTarget(Long domainId, Musician reporter) {
    Studio studio = studioRepository.findById(domainId)
        .orElseThrow(() -> new BusinessException(StudioErrorCode.STUDIO_NOT_FOUND));
  }

  @Override
  public JsonNode buildSnapshot(Long domainId) {
    Studio studio = getStudio(domainId);

    List<String> imageKeys = studio.getStudioImages().stream()
        .map(StudioImage::getImageKey)
        .filter(key -> key != null && !key.isBlank())
        .toList();

    Map<String, Object> snap = Map.of(
        "title", studio.getName(),
        "imageKeys", imageKeys,                       // imageUrls가 아니라 imageKeys로 저장
        "ownerNickname", studio.getOwner().getNickname()
    );

    try {
      return objectMapper.valueToTree(snap);
    } catch (Exception e) {
      log.error("Report snapshot serialize failed. studioId={}", domainId, e);
      throw new BusinessException(ReportErrorCode.REPORT_SNAPSHOT_SERIALIZE_FAILED);
    }
  }

  private Studio getStudio(Long domainId) {
    return studioRepository.findById(domainId)
        .orElseThrow(() -> new BusinessException(StudioErrorCode.STUDIO_NOT_FOUND));
  }
}
