package kr.muroom.muroombackendbach.studioboasting.application;

import java.util.List;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlsResponse;
import kr.muroom.muroombackendbach.studio.application.StudioService;
import kr.muroom.muroombackendbach.studio.exception.StudioErrorCode;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayStationInfo;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastImage;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastCommentRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastImageRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastRepository;
import kr.muroom.muroombackendbach.studioboasting.exception.StudioBoastErrorCode;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.CreateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.StudioBoastImageUploadRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse.CreatorUserInfo;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse.StudioInfo;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse.UnknownStudioInfo;
import kr.muroom.muroombackendbach.subway.application.SubwayService;
import kr.muroom.muroombackendbach.subway.presentation.dto.response.NearbyStationsResponse.StationInfo;
import kr.muroom.muroombackendbach.user.application.MusicianService;
import kr.muroom.muroombackendbach.user.application.UserService;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudioBoastService {

  private final StudioBoastRepository studioBoastRepository;
  private final StudioBoastImageRepository studioBoastImageRepository;
  private final StudioBoastCommentRepository studioBoastCommentRepository;
  private final FileStorageService fileStorageService;
  private final UserService userService;
  private final StudioService studioService;
  private final MusicianService musicianService;
  private final SubwayService subwayService;

  public GeneratePresignedPutUrlsResponse generateStudioImagePresignedPutUrls(
      List<StudioBoastImageUploadRequest> request) {
    return fileStorageService.generatePresignedPutUrls(request, FileStorageService::validateImageContentType);
  }

  @Transactional
  public Long createStudioBoast(CreateStudioBoastRequest request) {
    if (!userService.isValidMusicianId(request.creatorUserId())) {
      throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
    }

    if (!studioService.isValidStudioId(request.studioId())) {
      throw new BusinessException(StudioErrorCode.STUDIO_NOT_FOUND);
    }

    String thumbnailImageFileKey = request.imageFileKeys().getFirst();

    StudioBoast newStudioBoast = StudioBoast.builder()
        .content(request.content())
        .thumbnailImageFileKey(thumbnailImageFileKey)
        .studioName(request.studioName())
        .roadNameAddress(request.roadNameAddress())
        .lotNumberAddress(request.lotNumberAddress())
        .detailedAddress(request.detailedAddress())
        .instagramAccount(request.instagramAccount())
        .creatorUserId(request.creatorUserId())
        .studioId(request.studioId())
        .build();
    StudioBoast savedStudioBoast = studioBoastRepository.save(newStudioBoast);

    List<StudioBoastImage> newStudioBoastImages = request.imageFileKeys().stream()
        .map(imageFileKey -> StudioBoastImage.builder()
            .studioBoastId(savedStudioBoast.getId())
            .imageFileKey(fileStorageService.moveFromTempToPermanent(imageFileKey))
            .sequence(request.imageFileKeys().indexOf(imageFileKey) + 1)
            .build())
        .toList();
    studioBoastImageRepository.saveAll(newStudioBoastImages);

    return savedStudioBoast.getId();
  }

  public StudioBoastDetailResponse getStudioBoastDetail(Long studioBoastId) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));
    List<StudioBoastImage> studioBoastImages =
        studioBoastImageRepository.findByStudioBoastIdOrderBySequenceAsc(studioBoastId);
    List<String> studioBoastImageFileKeys = studioBoastImages.stream()
        .map(StudioBoastImage::getImageFileKey)
        .toList();

    Musician creatorUser = musicianService.getMusicianById(studioBoast.getCreatorUserId());
    CreatorUserInfo creatorUserInfo = CreatorUserInfo.builder()
        .id(creatorUser.getId())
        .nickname(creatorUser.getNickname())
        .instrument(creatorUser.getInstrument().getDescription())
        .build();

    boolean isStudioUploaded = studioBoast.getStudioId() != null;
    StudioInfo studioInfo = null;
    UnknownStudioInfo unknownStudioInfo = null;
    if (isStudioUploaded) {
      studioInfo = studioService.getStudioInfoById(studioBoast.getStudioId());
    } else {
      StationInfo nearestStationInfo = subwayService.findNearbyStations(studioBoast.getRoadNameAddress())
          .getStations()
          .getFirst();
      StudioSubwayStationInfo nearestSubwayStation = StudioSubwayStationInfo.builder()
          .stationName(nearestStationInfo.getStationName())
          .lines(nearestStationInfo.getLines())
          .distanceInMeters(nearestStationInfo.getDistanceInMeters())
          .build();
      unknownStudioInfo = UnknownStudioInfo.builder()
          .name(studioBoast.getStudioName())
          .nearestSubwayStation(nearestSubwayStation)
          .roadNameAddress(studioBoast.getRoadNameAddress())
          .lotNumberAddress(studioBoast.getLotNumberAddress())
          .detailedAddress(studioBoast.getDetailedAddress())
          .build();
    }

    return StudioBoastDetailResponse.builder()
        .id(studioBoast.getId())
        .content(studioBoast.getContent())
        .imageFileKeys(studioBoastImageFileKeys)
        .likeCount(studioBoast.getLikeCount())
        .commentCount(studioBoastCommentRepository.countByStudioBoastId(studioBoastId))
        .createdAt(studioBoast.getCreatedAt())
        .isStudioUploaded(isStudioUploaded)
        .creatorUserInfo(creatorUserInfo)
        .studioInfo(studioInfo)
        .unknownStudioInfo(unknownStudioInfo)
        .build();
  }

  @Transactional
  public void deleteStudioBoast(Long studioBoastId) {
    studioBoastRepository.deleteById(studioBoastId);
  }
}
