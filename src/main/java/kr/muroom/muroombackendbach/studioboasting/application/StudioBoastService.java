package kr.muroom.muroombackendbach.studioboasting.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.auth.exception.AuthErrorCode;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.studio.application.StudioService;
import kr.muroom.muroombackendbach.studio.exception.StudioErrorCode;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioListElementResponse;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastImage;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastCommentLikeRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastCommentRepository;
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
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastListElementResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastSimpleResponse;
import kr.muroom.muroombackendbach.subway.application.SubwayService;
import kr.muroom.muroombackendbach.subway.presentation.dto.response.NearbyStationsResponse;
import kr.muroom.muroombackendbach.user.application.MusicianService;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.domain.repository.MusicianRepository;
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
  private final StudioBoastCommentRepository studioBoastCommentRepository;
  private final StudioBoastCommentLikeRepository studioBoastCommentLikeRepository;
  private final MusicianRepository musicianRepository;

  public GeneratePresignedPutUrlResponse generateStudioImagePresignedPutUrl(
      StudioBoastImageUploadRequest request) {
    return fileStorageService.generatePresignedPutUrlForPublic(request,
        FileStorageService::validateImageContentType);
  }

  @Transactional
  public Long createStudioBoast(CreateStudioBoastRequest request, Long musicianId) {
    if (request.studioId() != null && !studioService.isExistingStudioId(request.studioId())) {
      throw new BusinessException(StudioErrorCode.STUDIO_NOT_FOUND);
    }

    if (request.instagramAccount() != null && !request.instagramAccount().isBlank()
        && !request.agreedToEventTerms()) {
      throw new BusinessException(StudioBoastErrorCode.EVENT_TERMS_NOT_AGREED);
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
        .agreedToEventTerms(request.agreedToEventTerms())
        .instagramAccount(request.instagramAccount())
        .creatorUserId(musicianId)
        .studioId(request.studioId())
        .build();
    StudioBoast savedStudioBoast = studioBoastRepository.save(newStudioBoast);

    List<StudioBoastImage> newStudioBoastImages = new ArrayList<>();
    for (int i = 0; i < permanentImageKeys.size(); i++) {
      newStudioBoastImages.add(StudioBoastImage.builder()
          .studioBoast(savedStudioBoast) // ID가 아닌 객체 자체를 주입
          .imageFileKey(permanentImageKeys.get(i))
          .sequence(i) // 0-based index 사용 (0이 썸네일)
          .build());
    }
    studioBoastImageRepository.saveAll(newStudioBoastImages);

    return savedStudioBoast.getId();
  }

  @Transactional
  public Long updateStudioBoast(Long studioBoastId, UpdateStudioBoastRequest request,
      Long currentUserId) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));

    if (!studioBoast.getCreatorUserId().equals(currentUserId)) {
      throw new BusinessException(AuthErrorCode.FORBIDDEN);
    }

    if (request.instagramAccount() != null && !request.instagramAccount().isBlank()
        && !request.agreedToEventTerms()) {
      throw new BusinessException(StudioBoastErrorCode.EVENT_TERMS_NOT_AGREED);
    }

    studioBoast.update(
        request.content(),
        request.studioName(),
        request.roadNameAddress(),
        request.lotNumberAddress(),
        request.detailedAddress(),
        request.agreedToEventTerms(),
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
    List<StudioBoastImage> oldImages =
        studioBoastImageRepository.findByStudioBoastOrderBySequenceAsc(
            studioBoast);
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
            .studioBoast(studioBoast)
            .imageFileKey(fileStorageService.movePublicFileFromTempToPermanent(imageFileKey))
            .sequence(i)
            .build());
      }
    }

    // 6. 변경된 이미지 목록을 한번에 저장
    studioBoastImageRepository.saveAll(imagesToUpdate);
  }

  public Page<StudioBoastListElementResponse> getStudioBoasts(Pageable pageable, Long musicianId) {
    Page<StudioBoast> studioBoastPage = studioBoastRepository.findAll(pageable);
    if (studioBoastPage.isEmpty()) {
      return Page.empty(pageable);
    }

    Set<Long> likedBoastIds;
    if (musicianId != null) {
      Musician requestMusician = musicianService.getMusicianById(musicianId);
      likedBoastIds = studioBoastLikeRepository.findAllByMusicianAndStudioBoastIn(requestMusician,
              studioBoastPage.getContent()).stream()
          .map(like -> like.getStudioBoast().getId())
          .collect(Collectors.toSet());
    } else {
      likedBoastIds = Collections.emptySet();
    }

    List<StudioBoastListElementResponse> studioBoastListElements = studioBoastPage.getContent()
        .stream()
        .map(studioBoast -> StudioBoastListElementResponse.builder()
            .id(String.valueOf(studioBoast.getId()))
            .thumbnailImageFileUrl(
                fileStorageService.getPublicFileUrl(studioBoast.getThumbnailImageFileKey())
            )
            .isLikedByRequestUser(likedBoastIds.contains(studioBoast.getId()))
            .build())
        .toList();

    return new PageImpl<>(studioBoastListElements, pageable, studioBoastPage.getTotalElements());
  }

  public Page<StudioBoastDetailResponse> getDetailedStudioBoasts(Pageable pageable, Long musicianId) {
    Page<StudioBoast> studioBoastPage = studioBoastRepository.findAll(pageable);
    if (studioBoastPage.isEmpty()) {
      return Page.empty(pageable);
    }
    List<StudioBoastDetailResponse> studioBoastDetails = enrichStudioBoasts(
        studioBoastPage.getContent(), musicianId);
    return new PageImpl<>(studioBoastDetails, pageable, studioBoastPage.getTotalElements());
  }

  public Page<StudioBoastDetailResponse> getRandomDetailedStudioBoasts(Pageable pageable, Long musicianId) {
    // 1. QueryDSL로 구현된 레포지토리의 findAllRandomly 메소드를 호출
    //    DB에서 직접 'ORDER BY RANDOM()'을 실행하여 무작위로 정렬된 데이터 페이지를 조회
    Page<StudioBoast> studioBoastPage = studioBoastRepository.findAllRandomly(pageable);

    // 2. 결과가 없으면 빈 페이지를 즉시 반환
    if (studioBoastPage.isEmpty()) {
      return Page.empty(pageable);
    }

    // 3. 기존의 enrichStudioBoasts 헬퍼 메소드를 재사용하여
    //    조회된 StudioBoast 엔티티 리스트를 StudioBoastDetailResponse DTO 리스트로 변환
    //    (작성자 정보, 이미지 URL, 좋아요 여부 등의 추가 정보를 채워넣는 과정)
    List<StudioBoastDetailResponse> studioBoastDetails = enrichStudioBoasts(
        studioBoastPage.getContent(), musicianId);

    // 4. 변환된 DTO 리스트와 페이지네이션 정보를 포함하는 새로운 Page 객체를 생성하여 반환
    return new PageImpl<>(studioBoastDetails, pageable, studioBoastPage.getTotalElements());
  }

  public Page<StudioBoastSimpleResponse> getSimpleStudioBoasts(Pageable pageable) {
    // 1. studioBoastRepository를 사용하여 페이지네이션된 StudioBoast 엔티티 목록을 조회합니다.
    //    Pageable 객체에 정렬 정보(최신순, 좋아요순)가 포함되어 있으므로 findAll(pageable)을 호출합니다.
    Page<StudioBoast> studioBoastPage = studioBoastRepository.findAll(pageable);

    // 2. 조회된 Page<StudioBoast> 객체의 내용을 Page<StudioBoastSimpleResponse>로 매핑합니다.
    //    - 각 StudioBoast 엔티티에 대해 StudioBoastSimpleResponse DTO를 생성합니다.
    //    - ID는 Long에서 String으로 변환하고, 썸네일 이미지 키는 FileStorageService를 통해
    //      접근 가능한 public URL로 변환합니다.
    return studioBoastPage.map(studioBoast -> StudioBoastSimpleResponse.builder()
        .id(String.valueOf(studioBoast.getId()))
        .thumbnailImageFileUrl(
            fileStorageService.getPublicFileUrl(studioBoast.getThumbnailImageFileKey())
        )
        .build());
  }

  public Page<StudioBoastDetailResponse> getMyStudioBoasts(Pageable pageable, Long musicianId) {
    Page<StudioBoast> studioBoastPage = studioBoastRepository.findAllByCreatorUserId(musicianId,
        pageable);
    if (studioBoastPage.isEmpty()) {
      return Page.empty(pageable);
    }
    List<StudioBoastDetailResponse> studioBoastDetails = enrichStudioBoasts(
        studioBoastPage.getContent(), musicianId);
    return new PageImpl<>(studioBoastDetails, pageable, studioBoastPage.getTotalElements());
  }

  private List<StudioBoastDetailResponse> enrichStudioBoasts(List<StudioBoast> studioBoasts,
      Long musicianId) {
    // 2. 후속 처리에 필요한 ID들을 일괄 수집
    List<Long> creatorUserIds = studioBoasts.stream().map(StudioBoast::getCreatorUserId).distinct()
        .toList();
    List<Long> studioIds = studioBoasts.stream()
        .map(StudioBoast::getStudioId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();

    // 3. 수집한 ID를 사용하여 연관 데이터 일괄 조회 (In-clause 쿼리)
    // 3-1. 이미지 정보 조회
    Map<Long, List<StudioBoastImage>> imagesByBoastId =
        studioBoastImageRepository.findAllByStudioBoastIn(
                studioBoasts).stream()
            .collect(
                Collectors.groupingBy(
                    studioBoastImage -> studioBoastImage.getStudioBoast().getId()));

    // 3-2. 작성자 정보 조회
    Map<Long, Musician> musiciansById = musicianService.getMusiciansAsMapByIds(creatorUserIds);

    // 3-3. 등록된 스튜디오 정보 조회
    Map<Long, StudioListElementResponse> studioInfosById = studioService.getStudioInfoByIds(
            studioIds).stream()
        .collect(
            Collectors.toMap(studio -> Long.parseLong(studio.studioId()), Function.identity()));

    List<String> unknownStudioAddresses = studioBoasts.stream()
        .filter(studioBoast -> studioBoast.getStudioId() == null)
        .map(StudioBoast::getRoadNameAddress)
        .distinct()
        .toList();
    Map<String, NearbyStationsResponse> nearbyStationsResult =
        subwayService.findNearbyStationsInBulk(
            unknownStudioAddresses);

    // 3-4. 현재 사용자의 '좋아요' 정보 조회
    final Set<Long> likedBoastIds;
    if (musicianId != null) {
      Musician requestMusician = musicianService.getMusicianById(musicianId);
      likedBoastIds = studioBoastLikeRepository.findAllByMusicianAndStudioBoastIn(requestMusician,
              studioBoasts).stream()
          .map(like -> like.getStudioBoast().getId())
          .collect(Collectors.toSet());
    } else {
      likedBoastIds = Collections.emptySet();
    }

    Map<Long, Long> commentCountsByStudioBoast =
        studioBoastCommentRepository.findCommentCountsByStudioBoastIn(
            studioBoasts);

    return studioBoasts.stream().map(studioBoast -> {
      // 이미지 URL 목록 생성
      List<String> imageUrls = imagesByBoastId.getOrDefault(studioBoast.getId(),
              Collections.emptyList())
          .stream()
          .sorted(Comparator.comparing(StudioBoastImage::getSequence))
          .map(image -> fileStorageService.getPublicFileUrl(image.getImageFileKey()))
          .toList();

      // 작성자 정보 DTO 생성
      Musician creator = musiciansById.get(studioBoast.getCreatorUserId());
      CreatorUserInfo creatorUserInfo;
      Boolean isWrittenByRequestUser = false;
      if (creator != null) {
        creatorUserInfo = CreatorUserInfo.builder()
            .id(String.valueOf(creator.getId()))
            .nickname(creator.getNickname())
            .instrument(creator.getInstrument().getDescription())
            .build();
        isWrittenByRequestUser = (musicianId != null) && musicianId.equals(creator.getId());
      } else {
        creatorUserInfo = CreatorUserInfo.builder()
            .id(null)
            .nickname("탈퇴한 사용자")
            .instrument(null)
            .build();
      }

      // 스튜디오 정보 DTO 생성 (등록/미등록 분기 처리)
      boolean isStudioUploaded = studioBoast.getStudioId() != null;
      StudioInfo studioInfo = null;
      UnknownStudioInfo unknownStudioInfo = null;
      if (isStudioUploaded) {
        StudioListElementResponse studioListElement = studioInfosById.get(
            studioBoast.getStudioId());
        if (studioListElement != null) {
          studioInfo = StudioInfo.from(studioListElement);
        }
      } else {
        unknownStudioInfo = UnknownStudioInfo.builder()
            .name(studioBoast.getStudioName())
            .roadNameAddress(studioBoast.getRoadNameAddress())
            .lotNumberAddress(studioBoast.getLotNumberAddress())
            .detailedAddress(studioBoast.getDetailedAddress())
            .build();
      }

      Long commentCount = commentCountsByStudioBoast.getOrDefault(studioBoast.getId(), 0L);

      // 최종 DTO 빌드
      return StudioBoastDetailResponse.builder()
          .id(String.valueOf(studioBoast.getId()))
          .content(studioBoast.getContent())
          .thumbnailImageFileUrl(
              fileStorageService.getPublicFileUrl(studioBoast.getThumbnailImageFileKey())
          )
          .imageFileUrls(imageUrls)
          .isWrittenByRequestUser(isWrittenByRequestUser)
          .isLikedByRequestUser(likedBoastIds.contains(studioBoast.getId()))
          .likeCount(studioBoast.getLikeCount())
          .commentCount(commentCount)
          .createdAt(studioBoast.getCreatedAt())
          .isStudioUploaded(isStudioUploaded)
          .creatorUserInfo(creatorUserInfo)
          .studioInfo(studioInfo)
          .unknownStudioInfo(unknownStudioInfo)
          .build();
    }).toList();
  }

  @Transactional(readOnly = true, noRollbackFor = BusinessException.class)
  public StudioBoastDetailResponse getStudioBoastDetail(Long studioBoastId, Long musicianId) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));
    List<StudioBoastImage> studioBoastImages =
        studioBoastImageRepository.findByStudioBoastOrderBySequenceAsc(studioBoast);
    List<String> studioBoastImageFileUrls = studioBoastImages.stream()
        .map(studioBoastImage -> fileStorageService.getPublicFileUrl(
            studioBoastImage.getImageFileKey()))
        .toList();

    // 작성자 정보 DTO 생성
    Optional<Musician> creatorOptional = musicianRepository.findById(
        studioBoast.getCreatorUserId());
    CreatorUserInfo creatorUserInfo;
    Boolean isWrittenByRequestUser = false;
    if (creatorOptional.isPresent()) {
      Musician creator = creatorOptional.get();
      creatorUserInfo = CreatorUserInfo.builder()
          .id(String.valueOf(creator.getId()))
          .nickname(creator.getNickname())
          .instrument(creator.getInstrument().getDescription())
          .build();
      isWrittenByRequestUser = (musicianId != null) && musicianId.equals(creator.getId());
    } else {
      creatorUserInfo = CreatorUserInfo.builder()
          .nickname("탈퇴한 사용자")
          .build();
    }

    boolean isStudioUploaded = studioBoast.getStudioId() != null;
    StudioInfo studioInfo = null;
    UnknownStudioInfo unknownStudioInfo = null;
    if (isStudioUploaded) {
      studioInfo = StudioInfo.from(studioService.getStudioInfoById(studioBoast.getStudioId()));
    } else {
      unknownStudioInfo = UnknownStudioInfo.builder()
          .name(studioBoast.getStudioName())
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

    long commentCount = studioBoastCommentRepository.countTopLevelCommentsByStudioBoast(studioBoast);

    return StudioBoastDetailResponse.builder()
        .id(String.valueOf(studioBoast.getId()))
        .content(studioBoast.getContent())
        .thumbnailImageFileUrl(
            fileStorageService.getPublicFileUrl(studioBoast.getThumbnailImageFileKey())
        )
        .imageFileUrls(studioBoastImageFileUrls)
        .isWrittenByRequestUser(isWrittenByRequestUser)
        .isLikedByRequestUser(isLiked)
        .likeCount(studioBoast.getLikeCount())
        .commentCount(commentCount)
        .createdAt(studioBoast.getCreatedAt())
        .isStudioUploaded(isStudioUploaded)
        .creatorUserInfo(creatorUserInfo)
        .studioInfo(studioInfo)
        .unknownStudioInfo(unknownStudioInfo)
        .build();
  }

  @Transactional
  public void deleteStudioBoast(Long studioBoastId, Long musicianId) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));

    if (!studioBoast.getCreatorUserId().equals(musicianId)) {
      throw new BusinessException(AuthErrorCode.FORBIDDEN);
    }

    // 1. 연관된 이미지 Soft Delete
    List<StudioBoastImage> studioBoastImages = studioBoastImageRepository.findAllByStudioBoast(
        studioBoast);
    if (studioBoastImages != null && !studioBoastImages.isEmpty()) {
      studioBoastImages.stream()
          .map(StudioBoastImage::getImageFileKey)
          .forEach(fileStorageService::deletePublicFile);
      studioBoastImageRepository.deleteAllByStudioBoast(studioBoast);
    }

    // 2. 연관된 '좋아요' 삭제
    studioBoastLikeRepository.deleteAllByStudioBoast(studioBoast);

    // 3. 연관된 댓글 삭제
    studioBoastCommentLikeRepository.deleteAllByStudioBoast(studioBoast);
    studioBoastCommentRepository.deleteAllByStudioBoast(studioBoast);

    // 4. 스튜디오 자랑글 자체 삭제
    studioBoastRepository.delete(studioBoast);
  }
}
