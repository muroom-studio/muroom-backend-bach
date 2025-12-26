package kr.muroom.muroombackendbach.report.handler.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.exception.ReportErrorCode;
import kr.muroom.muroombackendbach.report.handler.ReportTargetHandler;
import kr.muroom.muroombackendbach.studio.application.StudioService;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayStationInfo;
import kr.muroom.muroombackendbach.studioboasting.application.StudioBoastService;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastImage;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastImageRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastLikeRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastRepository;
import kr.muroom.muroombackendbach.studioboasting.exception.StudioBoastErrorCode;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse.CreatorUserInfo;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse.StudioInfo;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse.UnknownStudioInfo;
import kr.muroom.muroombackendbach.subway.application.SubwayService;
import kr.muroom.muroombackendbach.subway.presentation.dto.response.NearbyStationsResponse.StationInfo;
import kr.muroom.muroombackendbach.user.application.MusicianService;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StudioBoastReportTargetHandler implements ReportTargetHandler {

  private final StudioBoastRepository studioBoastRepository;
  private final StudioBoastImageRepository studioBoastImageRepository;
  private final StudioBoastService studioBoastService;
  private final ObjectMapper objectMapper;
  private final FileStorageService fileStorageService;
  private final StudioService studioService;
  private final MusicianService musicianService;
  private final SubwayService subwayService;
  private final StudioBoastLikeRepository studioBoastLikeRepository;

  @Override
  public ReportDomainType supports() {
    return ReportDomainType.STUDIO_BOAST;
  }

  @Override
  public void validateTarget(Long domainId, Musician reporter) {
    StudioBoast studioBoast = studioBoastRepository.findById(domainId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));

    if (studioBoast.getCreatorUserId().equals(reporter.getId())) {
      throw new BusinessException(ReportErrorCode.REPORT_NOT_ME);
    }
  }

  @Override
  public JsonNode buildSnapshot(Long domainId) {
    // 1. 기존 상세 조회 로직 재사용
    StudioBoastDetailResponse detailResponse = getStudioBoastDetail(domainId);
    // 2. DTO → JsonNode 변환
    return objectMapper.valueToTree(detailResponse);
  }

  public StudioBoastDetailResponse getStudioBoastDetail(Long studioBoastId) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));
    List<StudioBoastImage> studioBoastImages =
        studioBoastImageRepository.findByStudioBoastIdOrderBySequenceAsc(studioBoastId);
    List<String> studioBoastImageFileUrls = studioBoastImages.stream()
        .map(studioBoastImage -> fileStorageService.getPublicFileUrl(
            studioBoastImage.getImageFileKey()))
        .toList();

    Musician creatorUser = musicianService.getMusicianById(studioBoast.getCreatorUserId());
    CreatorUserInfo creatorUserInfo = CreatorUserInfo.builder()
        .id(String.valueOf(creatorUser.getId()))
        .nickname(creatorUser.getNickname())
        .instrument(creatorUser.getInstrument().getDescription())
        .agreedToEventTerms(studioBoast.isAgreedToEventTerms())
        .instagramAccount(studioBoast.getInstagramAccount())
        .build();

    boolean isStudioUploaded = studioBoast.getStudioId() != null;
    StudioInfo studioInfo = null;
    UnknownStudioInfo unknownStudioInfo = null;
    if (isStudioUploaded) {
      studioInfo = StudioInfo.from(studioService.getStudioInfoById(studioBoast.getStudioId()));
    } else {
      List<StationInfo> nearbySubwayStations = subwayService.findNearbyStations(
          studioBoast.getRoadNameAddress()).getStations();
      StudioSubwayStationInfo nearestSubwayStation = null;
      if (!nearbySubwayStations.isEmpty()) {
        StationInfo stationInfo = nearbySubwayStations.getFirst();
        nearestSubwayStation = StudioSubwayStationInfo.builder()
            .stationName(stationInfo.getStationName())
            .lines(stationInfo.getLines())
            .distanceInMeters(stationInfo.getDistanceInMeters())
            .build();
      }
      unknownStudioInfo = UnknownStudioInfo.builder()
          .name(studioBoast.getStudioName())
          .nearestSubwayStation(nearestSubwayStation)
          .roadNameAddress(studioBoast.getRoadNameAddress())
          .lotNumberAddress(studioBoast.getLotNumberAddress())
          .detailedAddress(studioBoast.getDetailedAddress())
          .build();
    }

    boolean isLiked = false;

    return StudioBoastDetailResponse.builder()
        .id(String.valueOf(studioBoast.getId()))
        .content(studioBoast.getContent())
        .thumbnailImageFileUrl(
            fileStorageService.getPublicFileUrl(studioBoast.getThumbnailImageFileKey())
        )
        .imageFileUrls(studioBoastImageFileUrls)
        .isLikedByRequestUser(isLiked)
        .likeCount(studioBoast.getLikeCount())
        .commentCount(0L)
        .createdAt(studioBoast.getCreatedAt())
        .isStudioUploaded(isStudioUploaded)
        .creatorUserInfo(creatorUserInfo)
        .studioInfo(studioInfo)
        .unknownStudioInfo(unknownStudioInfo)
        .build();
  }
}
