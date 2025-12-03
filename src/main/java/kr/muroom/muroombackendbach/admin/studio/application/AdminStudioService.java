package kr.muroom.muroombackendbach.admin.studio.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest.RoomInfoRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioImagePresignedUrlRequest;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService.PresignedPutUrlDto;
import kr.muroom.muroombackendbach.filestorage.exception.FileErrorCode;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedUrlsPutResponse;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedUrlsPutResponse.PresignedUrlInfo;
import kr.muroom.muroombackendbach.map.application.MapGeocodingService;
import kr.muroom.muroombackendbach.room.domain.entity.Room;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioBuildingInfo;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioForbiddenInstrument;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioImage;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioOption;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioPrice;
import kr.muroom.muroombackendbach.studio.domain.enums.StudioImageCategory;
import kr.muroom.muroombackendbach.studio.domain.repository.OptionRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioRepository;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStation;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationNearbyStudio;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayStationRepository;
import kr.muroom.muroombackendbach.subway.exception.SubwayErrorCode;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import kr.muroom.muroombackendbach.user.domain.repository.InstrumentRepository;
import kr.muroom.muroombackendbach.user.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminStudioService {

  private final StudioRepository studioRepository;
  private final OwnerRepository ownerRepository;
  private final MapGeocodingService mapGeocodingService;
  private final OptionRepository optionRepository;
  private final InstrumentRepository instrumentRepository;
  private final SubwayStationRepository subwayStationRepository;
  private final FileStorageService fileStorageService;

  public GeneratePresignedUrlsPutResponse generatePresignedPutUrls(StudioImagePresignedUrlRequest request) {
    List<PresignedUrlInfo> presignedUrlInfos = request.studioImages().stream()
        .map((studioImageInfo) -> {
          validateContentType(studioImageInfo.contentType());

          String domain = "studios/" + studioImageInfo.category().name().toLowerCase();
          PresignedPutUrlDto singleUrlDto = fileStorageService.generatePresignedPutUrl(
              studioImageInfo.fileName(), domain, studioImageInfo.contentType()
          );

          return new PresignedUrlInfo(singleUrlDto.url(), singleUrlDto.fileKey());
        })
        .toList();

    return new GeneratePresignedUrlsPutResponse(presignedUrlInfos);
  }

  private void validateContentType(String contentType) {
    if (!contentType.startsWith("image/")) {
      throw new BusinessException(FileErrorCode.UNSUPPORTED_FILE_TYPE);
    }
  }

  public Long createStudio(StudioCreateRequest request) {
    Owner owner = ownerRepository.findByPhoneNumber(request.ownerPhoneNumber())
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    boolean isRoadAddress = request.addressInfo().roadAddress() != null && !request.addressInfo().roadAddress().isBlank();
    String effectiveAddress = isRoadAddress
        ? request.addressInfo().roadAddress()
        : request.addressInfo().jibunAddress();
    Point location = mapGeocodingService.getPointFromAddress(effectiveAddress);

    String addressToStore = null;
    if (isRoadAddress) {
      addressToStore = request.addressInfo().roadAddress();
    } else if (request.addressInfo().jibunAddress() != null && !request.addressInfo().jibunAddress().isBlank()) {
      addressToStore = request.addressInfo().jibunAddress();
    }
    Studio studio = Studio.builder()
        .owner(owner)
        .name(request.studioName())
        .address(addressToStore)
        .detailedAddress(request.addressInfo().detailedAddress())
        .location(location)
        .introduction(request.introduction())
        .thumbnailImageKey(request.imageKeys().mainImageKeys().getFirst())
        .blueprintImageKey(request.imageKeys().blueprintImageKey())
        .build();

    studio.specifyBuildingInfo(buildStudioBuildingInfo(request.buildingInfo()));
    studio.specifyPrice(buildStudioPrice(request));
    studio.updateRooms(buildRooms(request.rooms()));
    studio.applyOptions(buildStudioOptions(request.optionCodes()));
    studio.updateForbiddenInstruments(buildForbiddenInstruments(request.forbiddenInstrumentCodes()));
    studio.updateImages(buildStudioImages(request.imageKeys()));
    studio.updateNearbyStations(buildNearbyStations(request.nearbyStations()));

    Studio savedStudio = studioRepository.save(studio);

    return savedStudio.getId();
  }

  private StudioBuildingInfo buildStudioBuildingInfo(StudioCreateRequest.BuildingInfoRequest request) {
    return StudioBuildingInfo.builder()
        .floorType(request.floorType())
        .floorNumber(request.floorNumber())
        .restroomType(request.restroomType())
        .isParkingAvailable(request.isParkingAvailable())
        .parkingFeeType(request.parkingFeeType())
        .parkingFeeInfo(request.parkingFeeInfo())
        .parkingSpots(request.parkingSpots())
        .parkingLocationAddress(request.parkingLocationAddress())
        .parkingLocationName(request.parkingLocationName())
        .isLodgingAvailable(request.isLodgingAvailable())
        .hasFireInsurance(request.hasFireInsurance())
        .build();
  }

  private StudioPrice buildStudioPrice(StudioCreateRequest request) {
    return StudioPrice.builder()
        .minPrice(request.studioMinPrice())
        .maxPrice(request.studioMaxPrice())
        .build();
  }

  private Set<Room> buildRooms(List<RoomInfoRequest> request) {
    if (request == null) {
      return new LinkedHashSet<>();
    }
    AtomicInteger sequence = new AtomicInteger(1);
    return request.stream()
        .map(roomReq -> Room.builder()
            .name(roomReq.roomName())
            .basePrice(roomReq.roomBasePrice())
            .isAvailable(roomReq.isAvailable())
            .availableAt(roomReq.availableAt())
            .width(roomReq.widthMm())
            .height(roomReq.heightMm())
            .sequence(sequence.getAndIncrement())
            .build())
        .collect(Collectors.toSet());
  }

  private Set<StudioOption> buildStudioOptions(List<String> optionCodes) {
    if (optionCodes == null) {
      return new HashSet<>();
    }
    return optionRepository.findAllByCodeIn(optionCodes).stream()
        .map(option -> StudioOption.builder().option(option).build())
        .collect(Collectors.toSet());
  }

  private Set<StudioForbiddenInstrument> buildForbiddenInstruments(List<String> instrumentCodes) {
    if (instrumentCodes == null) {
      return new HashSet<>();
    }
    return instrumentRepository.findAllByCodeIn(instrumentCodes).stream()
        .map(instrument -> StudioForbiddenInstrument.builder()
            .instrument(instrument)
            .build())
        .collect(Collectors.toSet());
  }

  private List<StudioImage> buildStudioImages(StudioCreateRequest.ImageKeysRequest request) {
    if (request == null) {
      return new ArrayList<>();
    }
    List<StudioImage> images = new ArrayList<>();

    AtomicInteger sequence = new AtomicInteger(1);
    request.mainImageKeys()
        .forEach(key -> images.add(StudioImage.builder()
            .category(StudioImageCategory.MAIN)
            .imageKey(key)
            .sequence(sequence.getAndIncrement())
            .build()));

    sequence.set(1); // 카테고리별로 순서 초기화
    if (request.buildingImageKeys() != null) {
      request.buildingImageKeys()
          .forEach(key -> images.add(StudioImage.builder()
              .category(StudioImageCategory.BUILDING)
              .imageKey(key)
              .sequence(sequence.getAndIncrement())
              .build()));
    }

    sequence.set(1);
    if (request.roomImageKeys() != null) {
      request.roomImageKeys()
          .forEach(key -> images.add(StudioImage.builder()
              .category(StudioImageCategory.ROOM)
              .imageKey(key)
              .sequence(sequence.getAndIncrement())
              .build()));
    }

    images.add(StudioImage.builder()
        .category(StudioImageCategory.BLUEPRINT)
        .imageKey(request.blueprintImageKey())
        .sequence(1)
        .build());

    return images;
  }

  private List<SubwayStationNearbyStudio> buildNearbyStations(List<StudioCreateRequest.NearbyStationRequest> request) {
    if (request == null) {
      return new ArrayList<>();
    }
    return request.stream()
        .map(stationReq -> {
          SubwayStation station = subwayStationRepository.findById(stationReq.subwayStationId())
              .orElseThrow(() -> new BusinessException(SubwayErrorCode.SUBWAY_NOT_FOUND));
          return SubwayStationNearbyStudio.builder()
              .subwayStation(station)
              .sequence(stationReq.sequence())
              .build();
        })
        .toList();
  }
}
