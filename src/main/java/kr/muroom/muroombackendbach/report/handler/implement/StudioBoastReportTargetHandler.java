package kr.muroom.muroombackendbach.report.handler.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.domain.FileStorageLocation;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.exception.ReportErrorCode;
import kr.muroom.muroombackendbach.report.handler.ReportTargetHandler;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastImage;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastImageRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastRepository;
import kr.muroom.muroombackendbach.studioboasting.exception.StudioBoastErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StudioBoastReportTargetHandler implements ReportTargetHandler {

  private final StudioBoastRepository studioBoastRepository;
  private final StudioBoastImageRepository studioBoastImageRepository;
  private final ObjectMapper objectMapper;
  private final FileStorageService fileStorageService;

  @Override
  public ReportDomainType supports() {
    return ReportDomainType.STUDIO_BOAST;
  }

  @Override
  public void validateTarget(Long studioBoastId, Musician reporter) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));

    if (studioBoast.getCreatorUserId().equals(reporter.getId())) {
      throw new BusinessException(ReportErrorCode.REPORT_NOT_ME);
    }
  }

  @Override
  public JsonNode buildSnapshot(Long studioBoastId) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));

    List<StudioBoastImage> images =
        studioBoastImageRepository.findAllByStudioBoast(studioBoast);

    // 1) 이미지: 원본 key 그대로 snapshot/report/{domain}/ 로 copy
    List<String> snapshotImageKeys = images.stream()
        .map(StudioBoastImage::getImageFileKey)
        .filter(key -> key != null && !key.isBlank())
        .map(key ->
            fileStorageService.copyToReportSnapshot(
                key,
                FileStorageLocation.PUBLIC_PERMANENT,
                supports().name().toLowerCase()
            )
        )
        .toList();

    // 2) snapshot 데이터 구성
    Map<String, Object> snapshot = Map.of(
        "boastId", studioBoast.getId(),
        "content", studioBoast.getContent(),
        "userId", studioBoast.getCreatorUserId(),
        "studioName", studioBoast.getStudioName(),
        "imageKeys", snapshotImageKeys,
        "roadNameAddress", studioBoast.getRoadNameAddress(),
        "lotNumberAddress", studioBoast.getLotNumberAddress(),
        "detailedAddress", studioBoast.getDetailedAddress(),
        "createdAt", studioBoast.getCreatedAt()
    );

    // 3) JsonNode 변환
    try {
      return objectMapper.valueToTree(snapshot);
    } catch (Exception e) {
      log.error("Report snapshot serialize failed. studioBoastId={}", studioBoastId, e);
      throw new BusinessException(ReportErrorCode.REPORT_SNAPSHOT_SERIALIZE_FAILED);
    }
  }

}
