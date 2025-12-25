package kr.muroom.muroombackendbach.studioboasting.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.auth.exception.AuthErrorCode;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.studio.application.StudioService;
import kr.muroom.muroombackendbach.studio.exception.StudioErrorCode;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayStationInfo;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioListElementResponse;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastImage;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastImageRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastLikeRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastRepository;
import kr.muroom.muroombackendbach.studioboasting.exception.StudioBoastErrorCode;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.CreateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.StudioBoastImageUploadRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.UpdateStudioBoastRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse.CreatorUserInfo;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse.StudioInfo;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse.UnknownStudioInfo;
import kr.muroom.muroombackendbach.subway.application.SubwayService;
import kr.muroom.muroombackendbach.subway.presentation.dto.response.NearbyStationsResponse;
import kr.muroom.muroombackendbach.subway.presentation.dto.response.NearbyStationsResponse.StationInfo;
import kr.muroom.muroombackendbach.user.application.MusicianService;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudioBoastService {

  private final StudioBoastRepository studioBoastRepository;
  private final StudioBoastImageRepository studioBoastImageRepository;
  // private final StudioBoastCommentRepository studioBoastCommentRepository;
  // private final StudioBoastLikeRepository studioBoastLikeRepository;
  private final FileStorageService fileStorageService;
  private final StudioService studioService;
  private final MusicianService musicianService;
  private final SubwayService subwayService;
  private final StudioBoastLikeRepository studioBoastLikeRepository;

  public GeneratePresignedPutUrlResponse generateStudioImagePresignedPutUrl(StudioBoastImageUploadRequest request) {
    return fileStorageService.generatePresignedPutUrlForPublic(request, FileStorageService::validateImageContentType);
  }

  @Transactional
  public Long createStudioBoast(CreateStudioBoastRequest request, Long musicianId) {
    if (request.studioId() != null && !studioService.isExistingStudioId(request.studioId())) {
      throw new BusinessException(StudioErrorCode.STUDIO_NOT_FOUND);
    }

    List<String> temporaryImageKeys = request.imageFileKeys();
    List<String> permanentImageKeys = temporaryImageKeys.stream()
        .map(fileStorageService::movePublicFileFromTempToPermanent)
        .toList();

    StudioBoast newStudioBoast = StudioBoast.builder()
        .content(request.content())
        .thumbnailImageFileKey(permanentImageKeys.getFirst())
        .studioName(request.studioName())
        .roadNameAddress(request.roadNameAddress())
        .lotNumberAddress(request.lotNumberAddress())
        .detailedAddress(request.detailedAddress())
        .instagramAccount(request.instagramAccount())
        .creatorUserId(musicianId)
        .studioId(request.studioId())
        .build();
    StudioBoast savedStudioBoast = studioBoastRepository.save(newStudioBoast);

    List<StudioBoastImage> newStudioBoastImages = new ArrayList<>();
    for (int i = 0; i < permanentImageKeys.size(); i++) {
      newStudioBoastImages.add(StudioBoastImage.builder()
          .studioBoastId(savedStudioBoast.getId()) // ID가 아닌 객체 자체를 주입
          .imageFileKey(permanentImageKeys.get(i))
          .sequence(i) // 0-based index 사용 (0이 썸네일)
          .build());
    }
    studioBoastImageRepository.saveAll(newStudioBoastImages);

    return savedStudioBoast.getId();
  }

  @Transactional
  public Long updateStudioBoast(Long studioBoastId, UpdateStudioBoastRequest request, Long currentUserId) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));

    if (!studioBoast.getCreatorUserId().equals(currentUserId)) {
      throw new BusinessException(AuthErrorCode.FORBIDDEN);
    }

    studioBoast.update(
        request.content(),
        request.studioName(),
        request.roadNameAddress(),
        request.lotNumberAddress(),
        request.detailedAddress(),
        request.instagramAccount(),
        request.studioId(),
        request.imageFileKeys().getFirst()
    );

    syncImages(studioBoast, request.imageFileKeys());

    return studioBoast.getId();
  }

  /**
   * 이미지 목록을 동기화하는 헬퍼 메소드 ('전체 교체' 방식)
   *
   * @param studioBoast      동기화 대상이 되는 StudioBoast 엔티티
   * @param newImageFileKeys 요청으로 들어온 새로운 이미지 키 목록
   */
  private void syncImages(StudioBoast studioBoast, List<String> newImageFileKeys) {

    // 1. DB에 저장된 기존 이미지 목록 조회
    List<StudioBoastImage> oldImages = studioBoastImageRepository.findByStudioBoastIdOrderBySequenceAsc(studioBoast.getId());
    Map<String, StudioBoastImage> oldImageMap = oldImages.stream()
        .collect(Collectors.toMap(StudioBoastImage::getImageFileKey, image -> image));

    // 2. 요청으로 들어온 새 이미지 키 Set 생성 (빠른 조회를 위해)
    Set<String> newImageFileKeySet = new HashSet<>(newImageFileKeys);

    // 3. 삭제할 이미지 결정: 기존 목록에는 있지만 새 목록에는 없는 이미지
    List<StudioBoastImage> imagesToDelete = oldImages.stream()
        .filter(oldImage -> !newImageFileKeySet.contains(oldImage.getImageFileKey()))
        .toList();

    // 4. 결정된 이미지들을 DB와 S3에서 삭제
    if (!imagesToDelete.isEmpty()) {
      studioBoastImageRepository.deleteAll(imagesToDelete);
      // S3에 저장된 파일도 삭제합니다.
      imagesToDelete.forEach(image -> fileStorageService.deletePublicFile(image.getImageFileKey()));
    }

    // 5. 새 이미지 목록 생성 및 업데이트 (순서 변경 및 추가 처리)
    List<StudioBoastImage> imagesToUpdate = new ArrayList<>();
    for (int i = 0; i < newImageFileKeys.size(); i++) {
      String imageFileKey = newImageFileKeys.get(i);
      StudioBoastImage studioBoastImage = oldImageMap.get(imageFileKey);

      if (studioBoastImage != null) {
        // 기존에 있던 이미지는 순서만 업데이트
        studioBoastImage.updateSequence(i);
        imagesToUpdate.add(studioBoastImage);
      } else {
        // 기존에 없던 새로운 이미지는 생성
        imagesToUpdate.add(StudioBoastImage.builder()
            .studioBoastId(studioBoast.getId())
            .imageFileKey(fileStorageService.movePublicFileFromTempToPermanent(imageFileKey))
            .sequence(i)
            .build());
      }
    }

    // 6. 변경된 이미지 목록을 한번에 저장
    studioBoastImageRepository.saveAll(imagesToUpdate);
  }

  public Page<StudioBoastDetailResponse> getStudioBoasts(Pageable pageable, Long musicianId) {
    // 1. 기본 데이터 페이지네이션 조회 (게시물)
    Page<StudioBoast> studioBoastPage = studioBoastRepository.findAll(pageable);
    List<StudioBoast> studioBoasts = studioBoastPage.getContent();

    if (studioBoasts.isEmpty()) {
      return Page.empty(pageable);
    }

    // 2. 후속 처리에 필요한 ID들을 일괄 수집
    List<Long> studioBoastIds = studioBoasts.stream().map(StudioBoast::getId).toList();
    List<Long> creatorUserIds = studioBoasts.stream().map(StudioBoast::getCreatorUserId).distinct().toList();
    List<Long> studioIds = studioBoasts.stream()
        .map(StudioBoast::getStudioId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();

    // 3. 수집한 ID를 사용하여 연관 데이터 일괄 조회 (In-clause 쿼리)
    // 3-1. 이미지 정보 조회
    Map<Long, List<StudioBoastImage>> imagesByBoastId = studioBoastImageRepository.findAllByStudioBoastIdIn(studioBoastIds)
        .stream()
        .collect(Collectors.groupingBy(StudioBoastImage::getStudioBoastId));

    // 3-2. 작성자 정보 조회
    Map<Long, Musician> musiciansById = musicianService.getMusiciansByIds(creatorUserIds)
        .stream()
        .collect(Collectors.toMap(Musician::getId, Function.identity()));

    // 3-3. 등록된 스튜디오 정보 조회
    Map<Long, StudioListElementResponse> studioInfosById = studioService.getStudioInfoByIds(studioIds)
        .stream()
        .collect(Collectors.toMap(studio -> Long.parseLong(studio.studioId()), Function.identity()));

    List<String> unknownStudioAddresses = studioBoasts.stream()
        .filter(studioBoast -> studioBoast.getStudioId() == null)
        .map(StudioBoast::getRoadNameAddress)
        .distinct()
        .toList();
    Map<String, NearbyStationsResponse> nearbyStationsResult = subwayService.findNearbyStationsInBulk(unknownStudioAddresses);

    // 3-4. 현재 사용자의 '좋아요' 정보 조회
    final Set<Long> likedBoastIds;
    if (musicianId != null) {
      Musician requestMusician = musicianService.getMusicianById(musicianId);
      likedBoastIds = studioBoastLikeRepository.findAllByMusicianAndStudioBoastIn(requestMusician, studioBoasts).stream()
          .map(like -> like.getStudioBoast().getId())
          .collect(Collectors.toSet());
    } else {
      likedBoastIds = Collections.emptySet();
    }

    // 4. 조회한 모든 데이터를 조합하여 최종 응답 DTO 리스트 생성
    List<StudioBoastDetailResponse> responses = studioBoasts.stream().map(boast -> {

      // 이미지 URL 목록 생성
      List<String> imageUrls = imagesByBoastId.getOrDefault(boast.getId(), Collections.emptyList())
          .stream()
          .sorted(Comparator.comparing(StudioBoastImage::getSequence))
          .map(image -> fileStorageService.getPublicFileUrl(image.getImageFileKey()))
          .toList();

      // 작성자 정보 DTO 생성
      Musician creator = musiciansById.get(boast.getCreatorUserId());
      CreatorUserInfo creatorUserInfo = CreatorUserInfo.builder()
          .id(String.valueOf(creator.getId()))
          .nickname(creator.getNickname())
          .instrument(creator.getInstrument().getDescription())
          .build();

      // 스튜디오 정보 DTO 생성 (등록/미등록 분기 처리)
      boolean isStudioUploaded = boast.getStudioId() != null;
      StudioInfo studioInfo = null;
      UnknownStudioInfo unknownStudioInfo = null;

      if (isStudioUploaded) {
        StudioListElementResponse studioListElement = studioInfosById.get(boast.getStudioId());
        if (studioListElement != null) {
          studioInfo = StudioInfo.from(studioListElement);
        }
      } else {
        NearbyStationsResponse nearbyStations = nearbyStationsResult.get(boast.getRoadNameAddress());
        StudioSubwayStationInfo nearestSubwayStation = null;

        if (nearbyStations != null && !nearbyStations.getStations().isEmpty()) {
          NearbyStationsResponse.StationInfo stationInfo = nearbyStations.getStations().getFirst();
          nearestSubwayStation = StudioSubwayStationInfo.builder()
              .stationName(stationInfo.getStationName())
              .lines(stationInfo.getLines())
              .distanceInMeters(stationInfo.getDistanceInMeters())
              .build();
        }
        unknownStudioInfo = UnknownStudioInfo.builder()
            .name(boast.getStudioName())
            .nearestSubwayStation(nearestSubwayStation)
            .roadNameAddress(boast.getRoadNameAddress())
            .lotNumberAddress(boast.getLotNumberAddress())
            .detailedAddress(boast.getDetailedAddress())
            .build();
      }

      // 최종 DTO 빌드
      return StudioBoastDetailResponse.builder()
          .id(String.valueOf(boast.getId()))
          .content(boast.getContent())
          .thumbnailImageFileUrl(
              fileStorageService.getPublicFileUrl(boast.getThumbnailImageFileKey())
          )
          .imageFileUrls(imageUrls)
          .isLikedByRequestUser(likedBoastIds.contains(boast.getId()))
          .likeCount(boast.getLikeCount())
          .commentCount(0L) // 댓글 기능은 아직 미구현
          .createdAt(boast.getCreatedAt())
          .isStudioUploaded(isStudioUploaded)
          .creatorUserInfo(creatorUserInfo)
          .studioInfo(studioInfo)
          .unknownStudioInfo(unknownStudioInfo)
          .build();
    }).toList();

    // 5. Page 객체로 변환하여 반환
    return new PageImpl<>(responses, pageable, studioBoastPage.getTotalElements());
  }

  public StudioBoastDetailResponse getStudioBoastDetail(Long studioBoastId, Long musicianId) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));
    List<StudioBoastImage> studioBoastImages =
        studioBoastImageRepository.findByStudioBoastIdOrderBySequenceAsc(studioBoastId);
    List<String> studioBoastImageFileUrls = studioBoastImages.stream()
        .map(studioBoastImage -> fileStorageService.getPublicFileUrl(studioBoastImage.getImageFileKey()))
        .toList();

    Musician creatorUser = musicianService.getMusicianById(studioBoast.getCreatorUserId());
    CreatorUserInfo creatorUserInfo = CreatorUserInfo.builder()
        .id(String.valueOf(creatorUser.getId()))
        .nickname(creatorUser.getNickname())
        .instrument(creatorUser.getInstrument().getDescription())
        .build();

    boolean isStudioUploaded = studioBoast.getStudioId() != null;
    StudioInfo studioInfo = null;
    UnknownStudioInfo unknownStudioInfo = null;
    if (isStudioUploaded) {
      studioInfo = StudioInfo.from(studioService.getStudioInfoById(studioBoast.getStudioId()));
    } else {
      List<StationInfo> nearbySubwayStations = subwayService.findNearbyStations(studioBoast.getRoadNameAddress()).getStations();
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
    if (musicianId != null) {
      Musician requestUser = musicianService.getMusicianById(musicianId);
      isLiked = studioBoastLikeRepository.existsByMusicianAndStudioBoast(requestUser, studioBoast);
    }

    return StudioBoastDetailResponse.builder()
        .id(String.valueOf(studioBoast.getId()))
        .content(studioBoast.getContent())
        .thumbnailImageFileUrl(
            fileStorageService.getPublicFileUrl(studioBoast.getThumbnailImageFileKey())
        )
        .imageFileUrls(studioBoastImageFileUrls)
        .isLikedByRequestUser(isLiked)
        .likeCount(studioBoast.getLikeCount())
        // studioBoastCommentRepository.countByStudioBoastId(studioBoastId)
        .commentCount(0L)
        .createdAt(studioBoast.getCreatedAt())
        .isStudioUploaded(isStudioUploaded)
        .creatorUserInfo(creatorUserInfo)
        .studioInfo(studioInfo)
        .unknownStudioInfo(unknownStudioInfo)
        .build();
  }

  @Transactional
  public void deleteStudioBoast(Long studioBoastId, Long musicianId) {
    // TODO: 삭제 권한 검증
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));

    if (!studioBoast.getCreatorUserId().equals(musicianId)) {
      throw new BusinessException(AuthErrorCode.FORBIDDEN);
    }

    // 1. 연관된 이미지 Soft Delete
    List<StudioBoastImage> studioBoastImages = studioBoastImageRepository.findAllByStudioBoastId(studioBoastId);
    if (studioBoastImages != null && !studioBoastImages.isEmpty()) {
      studioBoastImages.stream()
          .map(StudioBoastImage::getImageFileKey)
          .forEach(fileStorageService::deletePublicFile);
      studioBoastImageRepository.deleteAll(studioBoastImages);
    }

    // 2. 연관된 댓글 Soft Delete
    // studioBoastCommentRepository.softDeleteAllByStudioBoastId(studioBoastId);

    // 3. 연관된 '좋아요' Hard Delete
    studioBoastLikeRepository.deleteAllByStudioBoast(studioBoast);

    studioBoastRepository.deleteById(studioBoastId);
  }
}
