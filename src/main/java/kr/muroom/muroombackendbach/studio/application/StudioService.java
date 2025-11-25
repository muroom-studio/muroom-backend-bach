package kr.muroom.muroombackendbach.studio.application;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.map.application.MapDirectionService;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioPrice;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioPriceRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioRepository;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioRequest;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioResponse.LineInfo;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioResponse.MapList;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioResponse.SubwayStationInfo;
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
  private final StudioPriceRepository studioPriceRepository;
  private final SubwayStationsNearbyStudioRepository subwayStationsNearbyStudioRepository;
  private final SubwayStationLineRepository subwayStationLineRepository;
  private final FileStorageService fileStorageService;
  private final MapDirectionService mapDirectionService;

  public List<StudioResponse.MapBoundsSearch> searchStudiosInMapBounds(
      StudioRequest.MapBoundsSearch request) {
    List<Studio> studiosWithinBounds = studioRepository.findStudiosWithinBounds(
        request.minLatitude(), request.maxLatitude(),
        request.minLongitude(), request.maxLongitude()
    );

    return studiosWithinBounds.stream()
        .map(studio -> StudioResponse.MapBoundsSearch.from(studio))
        .toList();
  }
  
  public Page<MapList> searchStudiosForMapList(StudioRequest.MapBoundsSearch
          request,
      Pageable pageable) {
    Page<Studio> studioPage = studioRepository.findStudiosForMapList(
        request.minLatitude(), request.maxLatitude(),
        request.minLongitude(), request.maxLongitude(),
        pageable
    );

    List<Studio> studios = studioPage.getContent();
    if (studios.isEmpty()) {
      return Page.empty(pageable);
    }

    List<Long> studioIds = studios.stream().map(Studio::getId).toList();

    // 가격 정보 일괄 조회 (N+1 문제 해결)
    Map<Long, StudioPrice> pricesByStudioId = studioPriceRepository
        .findAllByStudioIdIn(studioIds).stream()
        .collect(Collectors.toMap(
            studioPrice -> studioPrice.getStudio().getId(),
            Function.identity()
        ));

    // (사장님이 설정한) 인증 지하철역 정보 일괄 조회 (N+1 문제 해결)
    Map<Long, SubwayStationNearbyStudio> nearbySubwayStationsByStudioId =
        subwayStationsNearbyStudioRepository.findAllByStudioIdInWithStation(
                studioIds).stream()
            .collect(
                Collectors.toMap(subwayStationNearby -> subwayStationNearby.getStudio().getId(),
                    Function.identity(),
                    (station1, station2) -> station1.getSequence() < station2.getSequence()
                        ? station1
                        : station2));

    // 지하철 노선 정보 일괄 조회 (N+1 문제 해결)
    List<Long> stationIds = nearbySubwayStationsByStudioId.values().stream()
        .map(nearbySubwayStation -> nearbySubwayStation.getSubwayStation().getId())
        .toList();
    Map<Long, List<StudioResponse.LineInfo>> lineInfosByStudioId =
        subwayStationLineRepository.findAllByStudioIdsWithLine(
                stationIds).stream()
            .collect(Collectors.groupingBy(
                subwayStationLine -> subwayStationLine.getStation().getId(),
                Collectors.mapping(subwayStationLine -> StudioResponse.LineInfo.builder()
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
    List<MapList> responseContent = IntStream.range(0, studios.size()).mapToObj(index -> {
      Studio studio = studios.get(index);
      Integer walkingTime = walkingTimeFutures.get(index).join();

      StudioPrice studioPrice = pricesByStudioId.get(studio.getId());
      Integer minPrice = studioPrice != null ? studioPrice.getMinPrice() : null;
      Integer maxPrice = studioPrice != null ? studioPrice.getMaxPrice() : null;

      SubwayStationNearbyStudio subwayStationNearbyStudio = nearbySubwayStationsByStudioId.get(
          studio.getId());
      SubwayStationInfo subwayStationInfo = null;
      if (subwayStationNearbyStudio != null) {
        SubwayStation subwayStation = subwayStationNearbyStudio.getSubwayStation();
        List<LineInfo> lineInfos = lineInfosByStudioId.getOrDefault(
            subwayStation.getId(), Collections.emptyList());
        subwayStationInfo = SubwayStationInfo.builder()
            .stationName(subwayStation.getName())
            .lines(lineInfos)
            .build();
      }

      String presignedThumbnailImageUrl = studio.getThumbnailImageKey() != null
          ? presignedUrls.get(studio.getThumbnailImageKey()) : null;

      return MapList.builder()
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
}
