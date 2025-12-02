package kr.muroom.muroombackendbach.studio.application;

import static kr.muroom.muroombackendbach.studio.exception.StudioErrorCode.STUDIO_NOT_FOUND;

import java.util.Collections;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.map.application.MapDirectionService;
import kr.muroom.muroombackendbach.room.domain.entity.Room;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioBuildingInfo;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioForbiddenInstrument;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioImage;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioOption;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioPrice;
import kr.muroom.muroombackendbach.studio.domain.enums.OptionCategory;
import kr.muroom.muroombackendbach.studio.domain.enums.StudioImageCategory;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioImageRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioOptionRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioRepository;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDetailResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDetailResponse.OptionDto;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDetailResponse.StudioBaseInfoDto;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDetailResponse.StudioBuildingInfoDto;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDetailResponse.StudioForbiddenInstrumentsDto;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDetailResponse.StudioNoticeDto;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDetailResponse.StudioOptionsDto;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDetailResponse.StudioRoomsDto;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayLineInfo;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayStationInfo;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStation;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationNearbyStudio;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayStationLineRepository;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayStationsNearbyStudioRepository;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudioDetailsService {

  private final StudioRepository studioRepository;
  private final SubwayStationsNearbyStudioRepository subwayStationsNearbyStudioRepository;
  private final SubwayStationLineRepository subwayStationLineRepository;
  private final StudioImageRepository studioImageRepository;
  private final StudioOptionRepository studioOptionRepository;

  private final FileStorageService fileStorageService;
  private final MapDirectionService mapDirectionService;
  private final StudioViewService studioViewService;

  private record PriceRange(Integer minPrice, Integer maxPrice) {

  }

  public StudioDetailResponse getStudio(Long studioId, Long musicianId) {
    // 1. 조회수 증가 로직 호출
    studioViewService.incrementViewCount(studioId, musicianId);

    // 2. Studio와 대부분의 연관 데이터를 한번의 쿼리로 조회
    Studio studio = studioRepository.findStudioDetailsById(studioId)
        .orElseThrow(() -> new BusinessException(STUDIO_NOT_FOUND));

    // 3. 별도 쿼리가 필요한 연관 데이터들을 조회 (N+1 방지)
    List<StudioImage> studioImages = studioImageRepository.findAllByStudio(studio);
    List<StudioOption> studioOptions = studioOptionRepository.findAllByStudio(studio);

    // 4. 나머지 데이터들을 변수에 할당
    Owner owner = studio.getOwner();
    StudioBuildingInfo studioBuildingInfo = studio.getStudioBuildingInfo();
    StudioPrice studioPrice = studio.getStudioPrice();
    Set<Room> rooms = studio.getRooms();
    Set<StudioForbiddenInstrument> forbiddenInstruments = studio.getForbiddenInstruments();

    PriceRange priceRange = calculateStudioPriceRange(studioPrice, rooms);
    List<StudioSubwayStationInfo> nearbySubwayStations = getNearbySubwayStations(studio);

    StudioBaseInfoDto studioBaseInfoDto = StudioBaseInfoDto.builder()
        .studioId(studio.getId())
        .studioName(studio.getName())
        .address(studio.getAddress())
        .studioMinPrice(priceRange.minPrice())
        .studioMaxPrice(priceRange.maxPrice())
        .nearbySubwayStations(nearbySubwayStations)
        .studioMainImageUrls(getPresignedUrlsForType(studioImages, StudioImageCategory.MAIN))
        .build();

    List<String> studioBuildingImageUrls = getPresignedUrlsForType(studioImages, StudioImageCategory.BUILDING);
    StudioBuildingInfoDto studioBuildingInfoDto = StudioBuildingInfoDto.from(studioBuildingInfo, studioBuildingImageUrls);

    StudioNoticeDto studioNoticeDto = StudioNoticeDto.from(owner, studio);

    StudioForbiddenInstrumentsDto studioForbiddenInstrumentsDto = StudioForbiddenInstrumentsDto.from(studio);

    List<String> roomImageUrls = getPresignedUrlsForType(studioImages, StudioImageCategory.ROOM);
    StudioRoomsDto studioRoomsDto = StudioRoomsDto.from(rooms, roomImageUrls);

    StudioOptionsDto studioOptionsDto = StudioOptionsDto.builder()
        .commonOptions(studioOptions.stream()
            .filter(studioOption -> studioOption.getOption().getCategory() == OptionCategory.COMMON)
            .map(studioOption -> OptionDto.from(studioOption.getOption()))
            .toList())
        .individualOptions(studioOptions.stream()
            .filter(studioOption -> studioOption.getOption().getCategory() == OptionCategory.INDIVIDUAL)
            .map(studioOption -> OptionDto.from(studioOption.getOption()))
            .toList())
        .build();

    return StudioDetailResponse.builder()
        .studioBaseInfo(studioBaseInfoDto)
        .studioBuildingInfo(studioBuildingInfoDto)
        .studioNotice(studioNoticeDto)
        .studioForbiddenInstruments(studioForbiddenInstrumentsDto)
        .studioRooms(studioRoomsDto)
        .studioOptions(studioOptionsDto)
        .build();
  }

  private PriceRange calculateStudioPriceRange(StudioPrice studioPrice, Set<Room> rooms) {
    Integer minPrice = null;
    Integer maxPrice = null;

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

    if (minPrice == null && studioPrice != null) {
      minPrice = studioPrice.getMinPrice();
      maxPrice = studioPrice.getMaxPrice();
    }

    return new PriceRange(minPrice, maxPrice);
  }

  private List<StudioSubwayStationInfo> getNearbySubwayStations(Studio studio) {
    // 1. 특정 스튜디오에 연결된 모든 '인근 지하철역' 엔티티를 순서대로 조회합니다.
    List<SubwayStationNearbyStudio> nearbyStations =
        subwayStationsNearbyStudioRepository.findAllByStudioOrderBySequenceAsc(studio);

    if (nearbyStations.isEmpty()) {
      return Collections.emptyList();
    }

    // 2. 지하철역들의 노선 정보를 한번의 쿼리로 모두 가져옵니다.
    List<Long> stationIds = nearbyStations.stream()
        .map(nearby -> nearby.getSubwayStation().getId())
        .toList();
    Map<Long, List<StudioSubwayLineInfo>> linesByStationId =
        subwayStationLineRepository.findAllByStudioIdsInWithLine(stationIds)
            .stream()
            .collect(Collectors.groupingBy(
                subwayStationLine -> subwayStationLine.getStation().getId(),
                Collectors.mapping(subwayStationLine -> StudioInfo.StudioSubwayLineInfo.builder()
                    .lineName(subwayStationLine.getLine().getName())
                    .lineColor(subwayStationLine.getLine().getColor())
                    .build(), Collectors.toList())
            ));

    // 3. 각 지하철역까지의 도보 시간을 병렬로 조회합니다. (API 동시 호출)
    List<CompletableFuture<StudioSubwayStationInfo>> futures = nearbyStations.stream()
        .map(nearby -> {
          SubwayStation station = nearby.getSubwayStation();
          // 위치 정보가 없으면 계산 불가
          if (studio.getLocation() == null) {
            return CompletableFuture.<StudioSubwayStationInfo>completedFuture(null);
          }

          // 각 역까지의 도보 시간을 비동기로 계산
          return mapDirectionService.getWalkingTimeMinutes(studio.getLocation(), station.getLocation())
              .thenApply(walkingTime -> StudioSubwayStationInfo.builder()
                  .stationName(station.getName())
                  .lines(linesByStationId.getOrDefault(station.getId(), Collections.emptyList()))
                  .walkingTimeMinutes(walkingTime)
                  .build());
        })
        .toList();

    // 4. 모든 도보 시간 계산이 완료될 때까지 기다린 후, 최종 리스트를 만듭니다.
    return futures.stream()
        .map(CompletableFuture::join)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private List<String> getPresignedUrlsForType(List<StudioImage> images, StudioImageCategory category) {
    if (images == null) {
      return Collections.emptyList();
    }

    List<String> keys = images.stream()
        .filter(image -> image.getCategory() == category)
        .sorted(Comparator.comparing(StudioImage::getSequence, Comparator.nullsLast(Comparator.naturalOrder())))
        .map(StudioImage::getImageKey)
        .toList();

    return keys.stream()
        .map(fileStorageService::generatePresignedGetUrl)
        .toList();
  }
}
