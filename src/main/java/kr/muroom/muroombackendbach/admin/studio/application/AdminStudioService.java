package kr.muroom.muroombackendbach.admin.studio.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest.RoomInfoRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioImageUploadRequest;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.instrument.domain.repository.InstrumentRepository;
import kr.muroom.muroombackendbach.map.application.MapGeocodingService;
import kr.muroom.muroombackendbach.owner.domain.application.OwnerService;
import kr.muroom.muroombackendbach.owner.domain.entity.Owner;
import kr.muroom.muroombackendbach.owner.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.room.domain.entity.Room;
import kr.muroom.muroombackendbach.room.domain.repository.RoomRepository;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioBuildingInfo;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioForbiddenInstrument;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioImage;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioOption;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioPrice;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomGender;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomLocation;
import kr.muroom.muroombackendbach.studio.domain.enums.StudioImageCategory;
import kr.muroom.muroombackendbach.studio.domain.repository.OptionRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioBuildingInfoRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioForbiddenInstrumentRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioImageRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioOptionRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioPriceRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioRepository;
import kr.muroom.muroombackendbach.studio.exception.StudioErrorCode;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStation;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationNearbyStudio;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayStationNearbyStudioRepository;
import kr.muroom.muroombackendbach.subway.domain.repository.SubwayStationRepository;
import kr.muroom.muroombackendbach.subway.exception.SubwayErrorCode;
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
  private final OptionRepository optionRepository;
  private final InstrumentRepository instrumentRepository;
  private final SubwayStationRepository subwayStationRepository;
  private final RoomRepository roomRepository;
  private final StudioPriceRepository studioPriceRepository;
  private final StudioBuildingInfoRepository studioBuildingInfoRepository;
  private final StudioImageRepository studioImageRepository;
  private final StudioOptionRepository studioOptionRepository;
  private final StudioForbiddenInstrumentRepository studioForbiddenInstrumentRepository;
  private final SubwayStationNearbyStudioRepository subwayStationNearbyStudioRepository;

  private final FileStorageService fileStorageService;
  private final MapGeocodingService mapGeocodingService;
  private final OwnerService ownerService;

  public GeneratePresignedPutUrlResponse generatePresignedPutUrl(StudioImageUploadRequest request) {
    return fileStorageService.generatePresignedPutUrlForPublic(request,
        FileStorageService::validateImageContentType);
  }

  public Long createStudio(StudioCreateRequest request) {
    Owner owner = ownerService.findByPhoneNumberOrThrowException(request.ownerPhoneNumber());

    String roadNameAddress = request.addressInfo().roadNameAddress();
    String lotNumberAddress = request.addressInfo().lotNumberAddress();
    Point location = mapGeocodingService.getPointFromAddress(roadNameAddress);

    List<StudioImage> studioImages = buildStudioImages(request.imageKeys());
    String thumbnailImageKey = studioImages.stream()
        .filter(
            image -> image.getCategory() == StudioImageCategory.MAIN && image.getSequence() == 1)
        .findFirst()
        .map(StudioImage::getImageKey)
        .orElse(null);
    String blueprintImageKey = studioImages.stream()
        .filter(image -> image.getCategory() == StudioImageCategory.BLUEPRINT)
        .findFirst()
        .map(StudioImage::getImageKey)
        .orElse(null);

    Studio prebuiltStudio = Studio.builder()
        .ownerId(owner.getId())
        .name(request.studioName())
        .roadNameAddress(roadNameAddress)
        .lotNumberAddress(lotNumberAddress)
        .detailedAddress(request.addressInfo().detailedAddress())
        .location(location)
        .introduction(request.introduction())
        .depositAmount(request.depositAmount())
        .thumbnailImageKey(thumbnailImageKey)
        .blueprintImageKey(blueprintImageKey)
        .build();

    Studio savedStudio = studioRepository.save(prebuiltStudio);

    if (request.studioMinPrice() != null || request.studioMaxPrice() != null) {
      if (request.studioMinPrice() != null && request.studioMaxPrice() != null
          && request.studioMinPrice() > request.studioMaxPrice()) {
        throw new BusinessException(StudioErrorCode.INVALID_PRICE_RANGE);
      }
      StudioPrice studioPrice = StudioPrice.builder()
          .studio(savedStudio)
          .minPrice(request.studioMinPrice())
          .maxPrice(request.studioMaxPrice())
          .build();
      studioPriceRepository.save(studioPrice);
    }

    StudioBuildingInfo buildingInfo = buildStudioBuildingInfo(request.buildingInfo());
    buildingInfo.assignStudio(savedStudio);
    studioBuildingInfoRepository.save(buildingInfo);

    Long studioId = savedStudio.getId();
    
    // Rooms 저장
    List<Room> rooms = buildRooms(request.rooms(), studioId);
    roomRepository.saveAll(rooms);

    // StudioOptions 저장
    Set<StudioOption> studioOptions = buildStudioOptions(request.optionCodes());
    studioOptions.forEach(option -> option.assignStudio(savedStudio));
    studioOptionRepository.saveAll(studioOptions);

    // ForbiddenInstruments 저장
    Set<StudioForbiddenInstrument> forbiddenInstruments = buildForbiddenInstruments(request.forbiddenInstrumentCodes());
    forbiddenInstruments.forEach(instrument -> instrument.assignStudio(savedStudio));
    studioForbiddenInstrumentRepository.saveAll(forbiddenInstruments);

    // StudioImages 저장 (메모리에 있던 리스트에 studio 정보 설정 후 저장)
    studioImages.forEach(image -> image.assignStudio(savedStudio));
    studioImageRepository.saveAll(studioImages);

    // NearbyStations 저장
    List<SubwayStationNearbyStudio> nearbyStations = buildNearbyStations(request.nearbyStations(), studioId);
    subwayStationNearbyStudioRepository.saveAll(nearbyStations);

    return savedStudio.getId();
  }

  private StudioBuildingInfo buildStudioBuildingInfo(StudioCreateRequest.BuildingInfoRequest request) {
    Boolean hasRestroom = request.hasRestroom();
    RestroomLocation restroomLocation = null;
    RestroomGender restroomGender = null;

    if (Boolean.TRUE.equals(hasRestroom)) {
      if (request.restroomLocation() == null && request.restroomGender() == null) {
        throw new BusinessException(StudioErrorCode.RESTROOM_DETAIL_IS_EMPTY);
      }

      restroomLocation = request.restroomLocation();
      restroomGender = request.restroomGender();
    }

    return StudioBuildingInfo.builder()
        .floorType(request.floorType())
        .floorNumber(request.floorNumber())
        .hasRestroom(hasRestroom)
        .restroomLocation(restroomLocation)
        .restroomGender(restroomGender)
        .parkingFeeType(request.parkingFeeType())
        .parkingFeeInfo(request.parkingFeeInfo())
        .parkingSpots(request.parkingSpots())
        .parkingLocationAddress(request.parkingLocationAddress())
        .parkingLocationName(request.parkingLocationName())
        .isLodgingAvailable(request.isLodgingAvailable())
        .hasFireInsurance(request.hasFireInsurance())
        .build();
  }

  private List<Room> buildRooms(List<RoomInfoRequest> request, Long studioId) {
    if (request == null) {
      return new ArrayList<>();
    }
    AtomicInteger sequence = new AtomicInteger(1);
    return request.stream()
        .map(roomReq -> Room.builder()
            .studioId(studioId) // studioId 설정
            .name(roomReq.roomName())
            .basePrice(roomReq.roomBasePrice())
            .isAvailable(roomReq.isAvailable())
            .availableAt(roomReq.availableAt())
            .width(roomReq.widthMm())
            .height(roomReq.heightMm())
            .sequence(sequence.getAndIncrement())
            .build())
        .toList();
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
            .imageKey(fileStorageService.movePublicFileFromTempToPermanent(key))
            .sequence(sequence.getAndIncrement())
            .build()));

    sequence.set(1); // 카테고리별로 순서 초기화
    if (request.buildingImageKeys() != null) {
      request.buildingImageKeys()
          .forEach(key -> images.add(StudioImage.builder()
              .category(StudioImageCategory.BUILDING)
              .imageKey(fileStorageService.movePublicFileFromTempToPermanent(key))
              .sequence(sequence.getAndIncrement())
              .build()));
    }

    sequence.set(1);
    if (request.roomImageKeys() != null) {
      request.roomImageKeys()
          .forEach(key -> images.add(StudioImage.builder()
              .category(StudioImageCategory.ROOM)
              .imageKey(fileStorageService.movePublicFileFromTempToPermanent(key))
              .sequence(sequence.getAndIncrement())
              .build()));
    }

    images.add(StudioImage.builder()
        .category(StudioImageCategory.BLUEPRINT)
        .imageKey(fileStorageService.movePublicFileFromTempToPermanent(request.blueprintImageKey()))
        .sequence(1)
        .build());

    sequence.set(1);
    if (request.commonOptionImageKeys() != null) {
      request.commonOptionImageKeys()
          .forEach(key -> images.add(StudioImage.builder()
              .category(StudioImageCategory.COMMON_OPTION)
              .imageKey(fileStorageService.movePublicFileFromTempToPermanent(key))
              .sequence(sequence.getAndIncrement())
              .build()));
    }

    sequence.set(1);
    if (request.individualOptionImageKeys() != null) {
      request.individualOptionImageKeys()
          .forEach(key -> images.add(StudioImage.builder()
              .category(StudioImageCategory.INDIVIDUAL_OPTION)
              .imageKey(fileStorageService.movePublicFileFromTempToPermanent(key))
              .sequence(sequence.getAndIncrement())
              .build()));
    }

    return images;
  }

  private List<SubwayStationNearbyStudio> buildNearbyStations(List<StudioCreateRequest.NearbyStationRequest> request, Long studioId) {
    if (request == null) {
      return new ArrayList<>();
    }
    return request.stream()
        .map(stationReq -> {
          SubwayStation station = subwayStationRepository.findById(stationReq.subwayStationId())
              .orElseThrow(() -> new BusinessException(SubwayErrorCode.SUBWAY_NOT_FOUND));
          return SubwayStationNearbyStudio.builder()
              .studioId(studioId)
              .subwayStation(station)
              .sequence(stationReq.sequence())
              .build();
        })
        .toList();
  }
}
