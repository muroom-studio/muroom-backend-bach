package kr.muroom.muroombackendbach.studio.application;

import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.util.SubjectParser;
import kr.muroom.muroombackendbach.common.util.SubjectParser.Subject;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.map.application.MapGeocodingService;
import kr.muroom.muroombackendbach.room.domain.entity.Room;
import kr.muroom.muroombackendbach.room.domain.repository.RoomRepository;
import kr.muroom.muroombackendbach.search.application.SearchHistoryService;
import kr.muroom.muroombackendbach.studio.domain.entity.Option;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioPrice;
import kr.muroom.muroombackendbach.studio.domain.enums.OptionCategory;
import kr.muroom.muroombackendbach.studio.domain.repository.OptionRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioPriceRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioRepository;
import kr.muroom.muroombackendbach.studio.exception.StudioErrorCode;
import kr.muroom.muroombackendbach.studio.presentation.dto.request.MapSearchRequest;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioAddressSearchResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioPriceInfo;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayLineInfo;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayStationInfo;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioListElementResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioMapResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStation;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationNearbyStudio;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayStationLineRepository;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayStationNearbyStudioRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudioService {

  private final StudioRepository studioRepository;
  private final StudioFavoriteService studioFavoriteService;
  private final RoomRepository roomRepository;
  private final StudioPriceRepository studioPriceRepository;
  private final SubwayStationNearbyStudioRepository subwayStationNearbyStudioRepository;
  private final SubwayStationLineRepository subwayStationLineRepository;
  private final OptionRepository optionRepository;

  private final FileStorageService fileStorageService;
  private final SearchHistoryService searchHistoryService;
  private final MapGeocodingService mapGeocodingService;

  public List<StudioMapResponse> searchStudiosInMapBounds(MapSearchRequest request,
      String subjectId) {
    MapSearchRequest resolvedRequest = resolveOptions(request);

    List<Studio> studiosWithinBounds = studioRepository.findStudiosWithinBounds(resolvedRequest);

    if (studiosWithinBounds.isEmpty()) {
      return Collections.emptyList();
    }

    // 2. N+1 문제를 해결하기 위해, 가격 계산에 필요한 정보를 일괄 조회합니다.
    List<Long> studioIds = studiosWithinBounds.stream().map(Studio::getId).toList();

    // 2-1. Room들의 가격 통계 정보를 일괄 조회합니다.
    Map<Long, IntSummaryStatistics> roomPriceStatsByStudioId = roomRepository.findAllByStudioIdIn(
            studioIds).stream()
        .collect(Collectors.groupingBy(
            Room::getStudioId,
            Collectors.mapping(Room::getBasePrice, Collectors.filtering(Objects::nonNull,
                Collectors.summarizingInt(Integer::intValue)))
        ));

    // 2-2. Room 가격 정보가 없을 경우를 대비해 StudioPrice 정보를 일괄 조회합니다.
    Map<Long, StudioPrice> studioPricesByStudioId = studioPriceRepository.findAllByStudioIdIn(
            studioIds).stream()
        .collect(Collectors.toMap(sp -> sp.getStudio().getId(), Function.identity()));

    // 3. 일괄 조회한 데이터를 사용하여 최종 DTO 목록을 생성합니다.
    return studiosWithinBounds.stream()
        .map(studio -> {
          // 미리 조회한 데이터를 사용하여 가격을 계산합니다.
          StudioPriceInfo studioPriceInfo = calculatePriceWithPrefetched(studio,
              roomPriceStatsByStudioId, studioPricesByStudioId);

          // StudioMapResponse를 빌드합니다.
          return StudioMapResponse.builder()
              .id(String.valueOf(studio.getId()))
              .name(studio.getName())
              .longitude(studio.getLocation().getX())
              .latitude(studio.getLocation().getY())
              .minPrice(studioPriceInfo.minPrice())
              .maxPrice(studioPriceInfo.maxPrice())
              .isFavorite(studioFavoriteService.isFavorite(studio.getId(), subjectId))
              .build();
        })
        .toList();
  }

  private StudioPriceInfo calculatePrice(Studio studio) {
    Integer minPrice = null;
    Integer maxPrice = null;

    List<Room> rooms = roomRepository.findAllByStudioId(studio.getId());

    if (rooms != null && !rooms.isEmpty()) {
      IntSummaryStatistics priceSummaryStats = rooms.stream()
          .filter(room -> room != null && room.getBasePrice() != null)
          .mapToInt(Room::getBasePrice)
          .summaryStatistics();

      if (priceSummaryStats.getCount() > 0) {
        minPrice = priceSummaryStats.getMin();
        maxPrice = priceSummaryStats.getMax();
      }
    }

    if (minPrice == null) {
      StudioPrice studioPrice = studioPriceRepository.findById(studio.getId()).orElse(null);
      if (studioPrice != null) {
        minPrice = studioPrice.getMinPrice();
        maxPrice = studioPrice.getMaxPrice();
      }
    }

    return StudioPriceInfo.builder()
        .minPrice(minPrice)
        .maxPrice(maxPrice)
        .build();
  }

  public Page<StudioListElementResponse> searchStudiosForMapList(MapSearchRequest request,
      String subjectId, Pageable pageable) {

    // 회원(U)인 경우에만 musicianId 추출 (비회원은 null 유지)
    Long musicianId = null;

    Subject subject = SubjectParser.parse(subjectId);
    if (subject != null && "U".equals(subject.prefix())) {
      musicianId = Long.valueOf(subject.id());
    }

    if (request.keyword() != null && !request.keyword().isBlank()) {
      searchHistoryService.addSearchKeyword(musicianId, request.keyword());
    }

    MapSearchRequest resolvedRequest = resolveOptions(request);

    Page<Studio> studioPage = studioRepository.findStudiosForMapList(resolvedRequest, pageable);

    List<Studio> studios = studioPage.getContent();
    if (studios.isEmpty()) {
      return Page.empty(pageable);
    }

    List<Long> studioIds = studios.stream().map(Studio::getId).toList();

    // 방 가격 통계 일괄 조회 (N+1 문제 해결, Studio fallback 가격과 비교용)
    Map<Long, IntSummaryStatistics> roomPriceStatsByStudioId = roomRepository.findAllByStudioIdIn(
            studioIds).stream()
        .collect(Collectors.groupingBy(
            Room::getStudioId,
            Collectors.mapping(Room::getBasePrice, Collectors.filtering(Objects::nonNull,
                Collectors.summarizingInt(Integer::intValue)))
        ));

    // 가격 정보 일괄 조회 (N+1 문제 해결, Room 가격 없을 경우 사용)
    Map<Long, StudioPrice> studioPricesByStudioId = studioPriceRepository
        .findAllByStudioIdIn(studioIds).stream()
        .collect(
            Collectors.toMap(studioPrice -> studioPrice.getStudio().getId(), Function.identity()));

    // (사장님이 설정한) 인증 지하철역 정보 일괄 조회 (N+1 문제 해결)
    Map<Long, SubwayStationNearbyStudio> nearbySubwayStationsByStudioId =
        subwayStationNearbyStudioRepository.findAllByStudioIdInWithStation(studioIds).stream()
            .collect(Collectors.toMap(
                SubwayStationNearbyStudio::getStudioId,
                Function.identity(),
                (station1, station2) -> station1.getSequence() < station2.getSequence()
                    ? station1 : station2
            ));

    // 지하철 노선 정보 일괄 조회 (N+1 문제 해결)
    List<Long> stationIds = nearbySubwayStationsByStudioId.values().stream()
        .map(nearbySubwayStation -> nearbySubwayStation.getSubwayStation().getId())
        .toList();
    Map<Long, List<StudioSubwayLineInfo>> lineInfosByStudioId =
        subwayStationLineRepository.findAllByStudioIdsInWithLine(
                stationIds).stream()
            .collect(Collectors.groupingBy(
                subwayStationLine -> subwayStationLine.getStation().getId(),
                Collectors.mapping(subwayStationLine -> StudioSubwayLineInfo.builder()
                    .lineName(subwayStationLine.getLine().getName())
                    .lineColor(subwayStationLine.getLine().getColor())
                    .build(), Collectors.toList())
            ));

    // Presigned URL 일괄 생성
    List<String> studioThumbnailImageKeys = studios.stream().map(Studio::getThumbnailImageKey)
        .filter(Objects::nonNull).toList();
    Map<String, String> presignedUrls = studioThumbnailImageKeys.stream()
        .collect(Collectors.toMap(studioThumbnailImageKey -> studioThumbnailImageKey,
            fileStorageService::getPublicFileUrl));

    // 정보 조합
    List<StudioListElementResponse> responseContent = studios.stream().map(studio -> {
      Integer minPrice = null;
      Integer maxPrice = null;
      IntSummaryStatistics roomPriceStats = roomPriceStatsByStudioId.get(studio.getId());
      if (roomPriceStats != null && roomPriceStats.getCount() > 0) {
        minPrice = roomPriceStats.getMin();
        maxPrice = roomPriceStats.getMax();
      } else {
        StudioPrice studioPrice = studioPricesByStudioId.get(studio.getId());
        if (studioPrice != null) {
          minPrice = studioPrice.getMinPrice();
          maxPrice = studioPrice.getMaxPrice();
        }
      }

      SubwayStationNearbyStudio subwayStationNearbyStudio = nearbySubwayStationsByStudioId.get(
          studio.getId());

      StudioSubwayStationInfo subwayStationInfo = null;

      if (subwayStationNearbyStudio != null) {
        SubwayStation subwayStation = subwayStationNearbyStudio.getSubwayStation();

        Integer distanceMeters = mapGeocodingService.calculateDistanceInMeters(studio.getLocation(),
            subwayStation.getLocation());

        List<StudioSubwayLineInfo> lineInfos = lineInfosByStudioId.getOrDefault(
            subwayStation.getId(),
            Collections.emptyList());
        subwayStationInfo = StudioSubwayStationInfo.builder()
            .stationName(subwayStation.getName())
            .lines(lineInfos)
            .distanceInMeters(distanceMeters)
            .build();
      }

      String presignedThumbnailImageUrl = studio.getThumbnailImageKey() != null
          ? presignedUrls.get(studio.getThumbnailImageKey())
          : null;

      Point location = studio.getLocation();
      Double longitude = location != null ? location.getX() : null;
      Double latitude = location != null ? location.getY() : null;

      return StudioListElementResponse.builder()
          .studioId(String.valueOf(studio.getId()))
          .studioName(studio.getName())
          .minPrice(minPrice)
          .maxPrice(maxPrice)
          .thumbnailImageUrl(presignedThumbnailImageUrl)
          .nearbySubwayStationInfo(subwayStationInfo)
          .longitude(longitude)
          .latitude(latitude)
          .isFavorite(studioFavoriteService.isFavorite(studio.getId(), subjectId))
          .build();
    }).toList();

    return new PageImpl<>(responseContent, pageable, studioPage.getTotalElements());
  }

  private MapSearchRequest resolveOptions(MapSearchRequest request) {
    Set<String> resolvedCommonOptionsCodes = request.commonOptionCodes();
    if (request.commonOptionCodes() != null && request.commonOptionCodes().size() == 1
        && "ALL".equalsIgnoreCase(request.commonOptionCodes().iterator().next())) {
      resolvedCommonOptionsCodes = optionRepository.findAllByCategory(OptionCategory.COMMON)
          .stream()
          .map(Option::getCode)
          .collect(Collectors.toSet());
    }

    Set<String> resolvedIndividualOptionsCodes = request.individualOptionCodes();
    if (request.individualOptionCodes() != null && request.individualOptionCodes().size() == 1
        && "ALL".equalsIgnoreCase(request.individualOptionCodes().iterator().next())) {
      resolvedIndividualOptionsCodes = optionRepository.findAllByCategory(OptionCategory.INDIVIDUAL)
          .stream()
          .map(Option::getCode)
          .collect(Collectors.toSet());
    }

    return MapSearchRequest.builder()
        .keyword(request.keyword())
        .minLatitude(request.minLatitude())
        .maxLatitude(request.maxLatitude())
        .minLongitude(request.minLongitude())
        .maxLongitude(request.maxLongitude())
        .commonOptionCodes(resolvedCommonOptionsCodes)
        .individualOptionCodes(resolvedIndividualOptionsCodes)
        .minPrice(request.minPrice())
        .maxPrice(request.maxPrice())
        .minRoomWidth(request.minRoomWidth())
        .maxRoomWidth(request.maxRoomWidth())
        .minRoomHeight(request.minRoomHeight())
        .maxRoomHeight(request.maxRoomHeight())
        .floorTypes(request.floorTypes())
        .restroomTypes(request.restroomTypes())
        .restroomLocations(request.restroomLocations())
        .restroomGenders(request.restroomGenders())
        .isParkingAvailable(request.isParkingAvailable())
        .isLodgingAvailable(request.isLodgingAvailable())
        .hasFireInsurance(request.hasFireInsurance())
        .forbiddenInstrumentCodes(request.forbiddenInstrumentCodes())
        .build();
  }

  public boolean isExistingStudioId(Long studioId) {
    return studioRepository.existsById(studioId);
  }

  /**
   * StudioId로 StudioInfo 조회
   *
   * <p>다른 서비스 에서 사용할 용도로만 존재하는 메서드입니다.
   */
  public StudioBoastDetailResponse.StudioInfo getStudioInfoById(Long studioId) {
    // TODO: 삭제된 스튜디오인 경우 처리해야됨
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new BusinessException(StudioErrorCode.STUDIO_NOT_FOUND));

    StudioPriceInfo studioPriceInfo = calculatePrice(studio);

    SubwayStationNearbyStudio subwayStationNearbyStudio = subwayStationNearbyStudioRepository
        .findFirstByStudioIdOrderBySequenceAsc(studioId);
    StudioSubwayStationInfo nearestSubwayStation = null;
    if (subwayStationNearbyStudio != null) {
      SubwayStation subwayStation = subwayStationNearbyStudio.getSubwayStation();
      Integer distanceInMeters = mapGeocodingService.calculateDistanceInMeters(studio.getLocation(),
          subwayStation.getLocation());
      List<StudioSubwayLineInfo> lines = subwayStationLineRepository.findAllByStationIdInWithLine(
              subwayStation.getId()).stream()
          .map(subwayStationLine -> StudioSubwayLineInfo.builder()
              .lineName(subwayStationLine.getLine().getName())
              .lineColor(subwayStationLine.getLine().getColor())
              .build())
          .toList();
      nearestSubwayStation = StudioSubwayStationInfo.builder()
          .stationName(subwayStation.getName())
          .lines(lines)
          .distanceInMeters(distanceInMeters)
          .build();
    }

    return StudioBoastDetailResponse.StudioInfo.builder()
        .id(String.valueOf(studio.getId()))
        .name(studio.getName())
        .thumbnailImageFileUrl(fileStorageService.getPublicFileUrl(studio.getThumbnailImageKey()))
        .roadNameAddress(studio.getRoadNameAddress())
        .lotNumberAddress(studio.getLotNumberAddress())
        .detailedAddress(studio.getDetailedAddress())
        .nearestSubwayStation(nearestSubwayStation)
        .minPrice(studioPriceInfo.minPrice())
        .maxPrice(studioPriceInfo.maxPrice())
        .build();
  }

  @Transactional(readOnly = true)
  public List<StudioBoastDetailResponse.StudioInfo> getStudioInfoByIds(List<Long> studioIds) {
    if (studioIds == null || studioIds.isEmpty()) {
      return Collections.emptyList();
    }

    // 단계 1: Studio 엔티티 일괄 조회
    // getStudioInfoById의 `studioRepository.findById`에 해당
    List<Studio> studios = studioRepository.findAllById(studioIds);
    if (studios.isEmpty()) {
      return Collections.emptyList();
    }

    // 단계 1-1: studio 결과를 ids 순서 유지
    Map<Long, Studio> studioById = studios.stream()
        .collect(Collectors.toMap(Studio::getId, Function.identity()));

    List<Studio> orderedStudios = studioIds.stream()
        .map(studioById::get)
        .filter(Objects::nonNull)
        .toList();

    // --- 사전 데이터 일괄 조회 (N+1 방지) ---

    // 단계 2를 위한 데이터: Room 가격, Studio 가격 정보 일괄 조회
    Map<Long, IntSummaryStatistics> roomPriceStatsByStudioId = roomRepository.findAllByStudioIdIn(
            studioIds).stream()
        .collect(Collectors.groupingBy(
            Room::getStudioId,
            Collectors.mapping(Room::getBasePrice, Collectors.filtering(Objects::nonNull,
                Collectors.summarizingInt(Integer::intValue)))));
    Map<Long, StudioPrice> studioPricesByStudioId = studioPriceRepository.findAllByStudioIdIn(
            studioIds).stream()
        .collect(Collectors.toMap(sp -> sp.getStudio().getId(), Function.identity()));

    // 단계 3을 위한 데이터: 가장 가까운 지하철역 정보 일괄 조회
    // getStudioInfoById의 `findFirstByStudioIdOrderBySequenceAsc`에 해당
    Map<Long, SubwayStationNearbyStudio> nearbyStationsByStudioId =
        subwayStationNearbyStudioRepository.findAllByStudioIdInWithStation(
                studioIds).stream()
            .collect(Collectors.toMap(
                SubwayStationNearbyStudio::getStudioId,
                Function.identity(),
                (s1, s2) -> s1.getSequence() < s2.getSequence() ? s1 : s2
            ));

    // 단계 4를 위한 데이터: 지하철 노선 정보 일괄 조회
    Set<Long> stationIds = nearbyStationsByStudioId.values().stream()
        .map(nearby -> nearby.getSubwayStation().getId())
        .collect(Collectors.toSet());

    final Map<Long, List<StudioSubwayLineInfo>> lineInfosByStationId = stationIds.isEmpty()
        ? Collections.emptyMap()
        : subwayStationLineRepository.findAllByStationIdsInWithLine(stationIds).stream()
            .collect(Collectors.groupingBy(line -> line.getStation().getId(),
                Collectors.mapping(line -> StudioSubwayLineInfo.builder()
                    .lineName(line.getLine().getName()).lineColor(line.getLine().getColor())
                    .build(), Collectors.toList())));

    // --- 최종 DTO 조립 ---
    return orderedStudios.stream().map(studio -> {

      // 단계 2: 가격 계산 (사전 조회된 데이터 사용)
      StudioPriceInfo studioPriceInfo = calculatePriceWithPrefetched(studio,
          roomPriceStatsByStudioId, studioPricesByStudioId);

      // 단계 3 & 4: 지하철역 정보 계산 (사전 조회된 데이터 사용)
      SubwayStationNearbyStudio subwayStationNearbyStudio = nearbyStationsByStudioId.get(
          studio.getId());
      StudioSubwayStationInfo nearestSubwayStation = null;
      if (subwayStationNearbyStudio != null) {
        SubwayStation subwayStation = subwayStationNearbyStudio.getSubwayStation();
        Integer distanceInMeters = mapGeocodingService.calculateDistanceInMeters(
            studio.getLocation(), subwayStation.getLocation());
        List<StudioSubwayLineInfo> lines = lineInfosByStationId.getOrDefault(subwayStation.getId(),
            Collections.emptyList());
        nearestSubwayStation = StudioSubwayStationInfo.builder()
            .stationName(subwayStation.getName())
            .lines(lines)
            .distanceInMeters(distanceInMeters)
            .build();
      }

      // 최종 빌드
      return StudioBoastDetailResponse.StudioInfo.builder()
          .id(String.valueOf(studio.getId()))
          .name(studio.getName())
          .thumbnailImageFileUrl(fileStorageService.getPublicFileUrl(studio.getThumbnailImageKey()))
          .roadNameAddress(studio.getRoadNameAddress())
          .lotNumberAddress(studio.getLotNumberAddress())
          .detailedAddress(studio.getDetailedAddress())
          .nearestSubwayStation(nearestSubwayStation)
          .minPrice(studioPriceInfo.minPrice())
          .maxPrice(studioPriceInfo.maxPrice())
          .build();
    }).toList();
  }

  private StudioPriceInfo calculatePriceWithPrefetched(
      Studio studio, Map<Long, IntSummaryStatistics> roomStatsMap,
      Map<Long, StudioPrice> studioPriceMap
  ) {
    Integer minPrice = null;
    Integer maxPrice = null;

    IntSummaryStatistics priceSummaryStats = roomStatsMap.get(studio.getId());
    if (priceSummaryStats != null && priceSummaryStats.getCount() > 0) {
      minPrice = priceSummaryStats.getMin();
      maxPrice = priceSummaryStats.getMax();
    }

    if (minPrice == null) {
      StudioPrice studioPrice = studioPriceMap.get(studio.getId());
      if (studioPrice != null) {
        minPrice = studioPrice.getMinPrice();
        maxPrice = studioPrice.getMaxPrice();
      }
    }

    return StudioPriceInfo.builder()
        .minPrice(minPrice)
        .maxPrice(maxPrice)
        .build();
  }

  public List<StudioAddressSearchResponse> getStudiosByRoadNameAddress(String roadNameAddress) {
    List<Studio> studios = studioRepository.findByRoadNameAddressContaining(roadNameAddress);
    return studios.stream()
        .map(studio -> StudioAddressSearchResponse.builder()
            .id(String.valueOf(studio.getId()))
            .name(studio.getName())
            .roadNameAddress(studio.getRoadNameAddress())
            .lotNumberAddress(studio.getLotNumberAddress())
            .detailedAddress(studio.getDetailedAddress())
            .build()
        ).toList();
  }
}
