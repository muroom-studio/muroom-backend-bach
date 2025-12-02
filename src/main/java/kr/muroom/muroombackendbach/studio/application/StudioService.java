package kr.muroom.muroombackendbach.studio.application;

import static kr.muroom.muroombackendbach.studio.exception.StudioErrorCode.STUDIO_NOT_FOUND;

import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.map.application.MapDirectionService;
import kr.muroom.muroombackendbach.room.domain.entity.Room;
import kr.muroom.muroombackendbach.room.domain.repository.RoomRepository;
import kr.muroom.muroombackendbach.studio.domain.entity.Option;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioPrice;
import kr.muroom.muroombackendbach.studio.domain.enums.OptionCategory;
import kr.muroom.muroombackendbach.studio.domain.repository.OptionRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioPriceRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioRepository;
import kr.muroom.muroombackendbach.studio.presentation.dto.request.MapSearchRequest;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDetailResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioPriceInfo;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayLineInfo;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayStationInfo;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioListResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioMapResponse;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStation;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationNearbyStudio;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayStationLineRepository;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayStationsNearbyStudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudioService {

  private final StudioRepository studioRepository;
  private final RoomRepository roomRepository;
  private final StudioPriceRepository studioPriceRepository;
  private final SubwayStationsNearbyStudioRepository subwayStationsNearbyStudioRepository;
  private final SubwayStationLineRepository subwayStationLineRepository;
  private final OptionRepository optionRepository;
  private final FileStorageService fileStorageService;
  private final MapDirectionService mapDirectionService;

  public List<StudioMapResponse> searchStudiosInMapBounds(MapSearchRequest request) {
    MapSearchRequest resolvedRequest = resolveOptionCodes(request);

    List<Studio> studiosWithinBounds = studioRepository.findStudiosWithinBounds(resolvedRequest);

    return studiosWithinBounds.stream()
        .map(this::convertToStudioMapResponse)
        .toList();
  }

  private StudioPriceInfo calculatePrice(Studio studio) {
    Integer minPrice = null;
    Integer maxPrice = null;

    if (studio.getRooms() != null && !studio.getRooms().isEmpty()) {
      IntSummaryStatistics priceSummaryStats = studio.getRooms().stream()
          .filter(room -> room != null && room.getBasePrice() != null)
          .mapToInt(Room::getBasePrice)
          .summaryStatistics();

      if (priceSummaryStats.getCount() > 0) {
        minPrice = priceSummaryStats.getMin();
        maxPrice = priceSummaryStats.getMax();
      }
    }

    if (minPrice == null) {
      StudioPrice studioPrice = studio.getStudioPrice();
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

  private StudioMapResponse convertToStudioMapResponse(Studio studio) {
    StudioPriceInfo studioPriceInfo = calculatePrice(studio);

    return StudioMapResponse.builder()
        .id(studio.getId())
        .name(studio.getName())
        .longitude(studio.getLocation().getX())
        .latitude(studio.getLocation().getY())
        .minPrice(studioPriceInfo.minPrice())
        .maxPrice(studioPriceInfo.maxPrice())
        .build();
  }

  public Page<StudioListResponse> searchStudiosForMapList(MapSearchRequest request, Pageable pageable) {
    MapSearchRequest resolvedRequest = resolveOptionCodes(request);

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
            room -> room.getStudio().getId(),
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
        subwayStationsNearbyStudioRepository.findAllByStudioIdInWithStation(studioIds).stream()
            .collect(Collectors.toMap(
                subwayStationNearby -> subwayStationNearby.getStudio().getId(),
                Function.identity(),
                (station1, station2) -> station1.getSequence() < station2.getSequence()
                    ? station1 : station2
            ));

    // 지하철 노선 정보 일괄 조회 (N+1 문제 해결)
    List<Long> stationIds = nearbySubwayStationsByStudioId.values().stream()
        .map(nearbySubwayStation -> nearbySubwayStation.getSubwayStation().getId())
        .toList();
    Map<Long, List<StudioSubwayLineInfo>> lineInfosByStudioId =
        subwayStationLineRepository.findAllByStudioIdsWithLine(
                stationIds).stream()
            .collect(Collectors.groupingBy(
                subwayStationLine -> subwayStationLine.getStation().getId(),
                Collectors.mapping(subwayStationLine -> StudioSubwayLineInfo.builder()
                    .lineName(subwayStationLine.getLine().getName())
                    .lineColor(subwayStationLine.getLine().getColor())
                    .build(), Collectors.toList())
            ));

    // 도보 시간 일괄 조회 (외부 API 호출 병렬 처리)
    List<CompletableFuture<Integer>> walkingTimeFutures = studios.stream().map(studio -> {
      SubwayStationNearbyStudio subwayStationNearbyStudio = nearbySubwayStationsByStudioId.get(
          studio.getId());
      if (subwayStationNearbyStudio == null) {
        return CompletableFuture.<Integer>completedFuture(null);
      } else {
        return mapDirectionService.getWalkingTimeMinutes(studio.getLocation(),
            subwayStationNearbyStudio.getSubwayStation().getLocation());
      }
    }).toList();
    CompletableFuture.allOf(walkingTimeFutures.toArray(new CompletableFuture[0])).join();

    // Presigned URL 일괄 생성
    List<String> studioThumbnailImageKeys = studios.stream().map(Studio::getThumbnailImageKey)
        .filter(Objects::nonNull).toList();
    Map<String, String> presignedUrls = studioThumbnailImageKeys.stream()
        .collect(Collectors.toMap(studioThumbnailImageKey -> studioThumbnailImageKey,
            fileStorageService::generatePresignedGetUrl));

    // 정보 조합
    List<StudioListResponse> responseContent = IntStream.range(0, studios.size())
        .mapToObj(index -> {
          Studio studio = studios.get(index);
          Integer walkingTime = walkingTimeFutures.get(index).join();

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
            List<StudioSubwayLineInfo> lineInfos = lineInfosByStudioId.getOrDefault(
                subwayStation.getId(),
                Collections.emptyList());
            subwayStationInfo = StudioSubwayStationInfo.builder()
                .stationName(subwayStation.getName())
                .lines(lineInfos).build();
          }

          String presignedThumbnailImageUrl = studio.getThumbnailImageKey() != null
              ? presignedUrls.get(studio.getThumbnailImageKey())
              : null;

          return StudioListResponse.builder()
              .studioId(studio.getId())
              .studioName(studio.getName())
              .minPrice(minPrice)
              .maxPrice(maxPrice)
              .thumbnailImageUrl(presignedThumbnailImageUrl)
              .nearbySubwayStationInfo(subwayStationInfo)
              .walkingTimeMinutes(walkingTime)
              .build();
        }).toList();

    return new PageImpl<>(responseContent, pageable, studioPage.getTotalElements());
  }

  private MapSearchRequest resolveOptionCodes(MapSearchRequest request) {
    List<String> resolvedCommonOptionsCodes = request.commonOptionCodes();
    if (request.commonOptionCodes() != null && request.commonOptionCodes().size() == 1
        && "ALL".equalsIgnoreCase(request.commonOptionCodes().getFirst())) {
      resolvedCommonOptionsCodes = optionRepository.findAllByCategory(OptionCategory.COMMON)
          .stream()
          .map(Option::getCode)
          .toList();
    }

    List<String> resolvedIndividualOptionsCodes = request.individualOptionCodes();
    if (request.individualOptionCodes() != null && request.individualOptionCodes().size() == 1
        && "ALL".equalsIgnoreCase(request.individualOptionCodes().getFirst())) {
      resolvedIndividualOptionsCodes = optionRepository.findAllByCategory(OptionCategory.INDIVIDUAL)
          .stream()
          .map(Option::getCode)
          .toList();
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
        .isParkingAvailable(request.isParkingAvailable())
        .isLodgingAvailable(request.isLodgingAvailable())
        .hasFireInsurance(request.hasFireInsurance())
        .forbiddenInstrumentCodes(request.forbiddenInstrumentCodes())
        .build();
  }

  public StudioDetailResponse getStudio(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new BusinessException(STUDIO_NOT_FOUND));

    return null;
  }
}
