package kr.muroom.muroombackendbach.studio.application.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest.BuildingInfoRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest.ImageKeysRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest.NearbyStationRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest.RoomInfoRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioImageUploadRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioUpdateRequest;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.domain.FileStorageLocation;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.instrument.domain.repository.InstrumentRepository;
import kr.muroom.muroombackendbach.map.application.MapGeocodingService;
import kr.muroom.muroombackendbach.owner.domain.application.OwnerService;
import kr.muroom.muroombackendbach.owner.domain.entity.Owner;
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
public class StudioCommandService {

  private final StudioRepository studioRepository;
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
    return fileStorageService.getUploadUrl(FileStorageLocation.PUBLIC_TEMP, request);
  }

  public Long createStudio(StudioCreateRequest request) {
    Owner owner = ownerService.findByPhoneNumberOrThrowException(request.ownerPhoneNumber());

    Point location = mapGeocodingService.getPointFromAddress(
        request.addressInfo().roadNameAddress());
    List<StudioImage> studioImages = buildStudioImagesForCreate(request.imageKeys());

    String thumbnailImageKey = studioImages.stream()
        .filter(
            image -> image.getCategory() == StudioImageCategory.MAIN && image.getSequence() == 1)
        .findFirst()
        .map(StudioImage::getImageKey)
        .orElseThrow(() -> new BusinessException(StudioErrorCode.THUMBNAIL_IMAGE_NOT_FOUND));
    String blueprintImageKey = studioImages.stream()
        .filter(image -> image.getCategory() == StudioImageCategory.BLUEPRINT)
        .findFirst()
        .map(StudioImage::getImageKey)
        .orElseThrow(() -> new BusinessException(StudioErrorCode.BLUEPRINT_IMAGE_NOT_FOUND));

    Studio prebuiltStudio = Studio.builder()
        .ownerId(owner.getId())
        .name(request.studioName())
        .roadNameAddress(request.addressInfo().roadNameAddress())
        .lotNumberAddress(request.addressInfo().lotNumberAddress())
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

    Long studioId = savedStudio.getId();
    StudioBuildingInfo buildingInfo = buildStudioBuildingInfo(request.buildingInfo());
    buildingInfo.assignStudio(savedStudio);
    studioBuildingInfoRepository.save(buildingInfo);

    List<Room> rooms = buildRooms(request.rooms(), studioId);
    roomRepository.saveAll(rooms);

    Set<StudioOption> studioOptions = buildStudioOptions(request.optionCodes(), savedStudio);
    studioOptionRepository.saveAll(studioOptions);

    Set<StudioForbiddenInstrument> forbiddenInstruments = buildForbiddenInstruments(
        request.forbiddenInstrumentCodes(), savedStudio);
    studioForbiddenInstrumentRepository.saveAll(forbiddenInstruments);

    studioImages.forEach(image -> image.assignStudio(savedStudio));
    studioImageRepository.saveAll(studioImages);

    List<SubwayStationNearbyStudio> nearbyStations = buildNearbyStations(
        request.nearbyStations(), studioId);
    subwayStationNearbyStudioRepository.saveAll(nearbyStations);

    return savedStudio.getId();
  }

  public void updateStudio(Long studioId, StudioUpdateRequest request) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new BusinessException(StudioErrorCode.STUDIO_NOT_FOUND));

    // --- 1. 스튜디오 하위 엔티티들 업데이트 ---
    updateStudioPrice(studio, request.studioMinPrice(), request.studioMaxPrice());
    updateStudioBuildingInfo(studio, request.buildingInfo());
    updateRooms(studio, request.rooms());
    updateOptions(studio, request.optionCodes());
    updateForbiddenInstruments(studio, request.forbiddenInstrumentCodes());
    updateNearbyStations(studio, request.nearbyStations());

    List<StudioImage> finalImages = updateImages(studio, request.imageKeys());

    // --- 2. 스튜디오 기본 정보 및 위치, 썸네일/설계도 정보 업데이트 ---
    Point newLocation = mapGeocodingService.getPointFromAddress(
        request.addressInfo().roadNameAddress());
    String newThumbnailImageKey = finalImages.stream()
        .filter(image -> image.getCategory() == StudioImageCategory.MAIN && image.getSequence() == 1)
        .findFirst()
        .map(StudioImage::getImageKey).orElse(null);
    String newBlueprintImageKey = finalImages.stream()
        .filter(image -> image.getCategory() == StudioImageCategory.BLUEPRINT)
        .findFirst()
        .map(StudioImage::getImageKey).orElse(null);

    studio.update(
        request.studioName(),
        request.addressInfo().roadNameAddress(),
        request.addressInfo().lotNumberAddress(),
        request.addressInfo().detailedAddress(),
        newLocation,
        request.introduction(),
        request.depositAmount(),
        newThumbnailImageKey,
        newBlueprintImageKey
    );
  }

  private void updateStudioPrice(Studio studio, Integer minPrice, Integer maxPrice) {
    if (minPrice != null || maxPrice != null) {
      if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
        throw new BusinessException(StudioErrorCode.INVALID_PRICE_RANGE);
      }
      StudioPrice priceToUpdate = studioPriceRepository.findById(studio.getId())
          .orElseGet(() -> StudioPrice.builder().studio(studio).build());
      priceToUpdate.update(minPrice, maxPrice);
      studioPriceRepository.save(priceToUpdate);
    } else {
      studioPriceRepository.deleteById(studio.getId());
    }
  }

  private void updateStudioBuildingInfo(Studio studio, BuildingInfoRequest buildingInfoRequest) {
    StudioBuildingInfo buildingInfoToUpdate = studioBuildingInfoRepository.findById(studio.getId())
        .orElseGet(() -> {
          StudioBuildingInfo newInfo = buildStudioBuildingInfo(buildingInfoRequest);
          newInfo.assignStudio(studio);
          return newInfo;
        });
    StudioBuildingInfo newBuildingInfoData = buildStudioBuildingInfo(buildingInfoRequest);
    buildingInfoToUpdate.update(newBuildingInfoData.getFloorType(),
        newBuildingInfoData.getFloorNumber(),
        newBuildingInfoData.getHasRestroom(), newBuildingInfoData.getRestroomLocation(),
        newBuildingInfoData.getRestroomGender(),
        newBuildingInfoData.getParkingFeeType(), newBuildingInfoData.getParkingFeeInfo(),
        newBuildingInfoData.getParkingSpots(),
        newBuildingInfoData.getParkingLocationName(),
        newBuildingInfoData.getParkingLocationAddress(),
        newBuildingInfoData.getIsLodgingAvailable(), newBuildingInfoData.getHasFireInsurance());
    studioBuildingInfoRepository.save(buildingInfoToUpdate);
  }

  private void updateRooms(Studio studio, List<RoomInfoRequest> roomRequests) {
    List<Room> existingRooms = roomRepository.findAllByStudioId(studio.getId());
    Map<String, Room> existingRoomsMap = existingRooms.stream()
        .collect(Collectors.toMap(Room::getName, room -> room));

    List<Room> roomsToSave = new ArrayList<>();
    Set<String> processedRoomNames = new HashSet<>();

    if (roomRequests != null) {
      AtomicInteger sequenceCounter = new AtomicInteger(1);
      for (RoomInfoRequest roomReq : roomRequests) {
        String roomName = roomReq.roomName();
        processedRoomNames.add(roomName);
        int currentSequence = sequenceCounter.getAndIncrement();

        Room existingRoom = existingRoomsMap.get(roomName);
        if (existingRoom != null) {
          existingRoom.update(currentSequence, roomReq.widthMm(), roomReq.heightMm(),
              roomReq.isAvailable(), roomReq.availableAt(), roomReq.basePrice());
          roomsToSave.add(existingRoom);
        } else {
          roomsToSave.add(Room.builder()
              .studioId(studio.getId())
              .name(roomName)
              .sequence(currentSequence)
              .width(roomReq.widthMm())
              .height(roomReq.heightMm())
              .isAvailable(roomReq.isAvailable())
              .availableAt(roomReq.availableAt())
              .basePrice(roomReq.basePrice())
              .build());
        }
      }
    }

    List<Room> roomsToDelete = existingRooms.stream()
        .filter(room -> !processedRoomNames.contains(room.getName()))
        .toList();

    roomRepository.saveAll(roomsToSave);
    roomRepository.deleteAll(roomsToDelete);
  }

  private record CategorizedImageKey(StudioImageCategory category, String key, int sequence) {

  }

  private List<StudioImage> updateImages(Studio studio, ImageKeysRequest imageKeysRequest) {
    if (imageKeysRequest == null) {
      List<StudioImage> existingImages = studioImageRepository.findAllByStudio(studio);
      studioImageRepository.deleteAll(existingImages);
      existingImages.forEach(
          image -> fileStorageService.softDelete(image.getImageKey(), FileStorageLocation.PUBLIC_PERMANENT));
      return new ArrayList<>();
    }

    List<CategorizedImageKey> requestedImageKeys = new ArrayList<>();
    AtomicInteger seq = new AtomicInteger(1);
    imageKeysRequest.mainImageKeys().forEach(key -> requestedImageKeys.add(
        new CategorizedImageKey(StudioImageCategory.MAIN, key, seq.getAndIncrement())));
    seq.set(1);
    if (imageKeysRequest.buildingImageKeys() != null) {
      imageKeysRequest.buildingImageKeys().forEach(key -> requestedImageKeys.add(
          new CategorizedImageKey(StudioImageCategory.BUILDING, key, seq.getAndIncrement())));
    }
    seq.set(1);
    if (imageKeysRequest.roomImageKeys() != null) {
      imageKeysRequest.roomImageKeys().forEach(key -> requestedImageKeys.add(
          new CategorizedImageKey(StudioImageCategory.ROOM, key, seq.getAndIncrement())));
    }
    requestedImageKeys.add(
        new CategorizedImageKey(StudioImageCategory.BLUEPRINT, imageKeysRequest.blueprintImageKey(),
            1));
    seq.set(1);
    if (imageKeysRequest.commonOptionImageKeys() != null) {
      imageKeysRequest.commonOptionImageKeys().forEach(key -> requestedImageKeys.add(
          new CategorizedImageKey(StudioImageCategory.COMMON_OPTION, key,
              seq.getAndIncrement())));
    }
    seq.set(1);
    if (imageKeysRequest.individualOptionImageKeys() != null) {
      imageKeysRequest.individualOptionImageKeys().forEach(key -> requestedImageKeys.add(
          new CategorizedImageKey(StudioImageCategory.INDIVIDUAL_OPTION, key,
              seq.getAndIncrement())));
    }

    Map<String, StudioImage> existingImagesMap = studioImageRepository.findAllByStudio(studio)
        .stream()
        .collect(Collectors.toMap(StudioImage::getImageKey, image -> image));

    List<StudioImage> finalImages = new ArrayList<>();
    Set<String> requestedKeysSet = requestedImageKeys.stream().map(CategorizedImageKey::key)
        .collect(Collectors.toSet());

    for (CategorizedImageKey req : requestedImageKeys) {
      if (existingImagesMap.containsKey(req.key())) {
        StudioImage existingImage = existingImagesMap.get(req.key());
        existingImage.updateCategoryAndSequence(req.category(), req.sequence());
        finalImages.add(existingImage);
      } else {
        String permanentKey = fileStorageService.move(req.key(), FileStorageLocation.PUBLIC_TEMP, FileStorageLocation.PUBLIC_PERMANENT);
        finalImages.add(StudioImage.builder()
            .studio(studio)
            .category(req.category())
            .imageKey(permanentKey)
            .sequence(req.sequence())
            .build());
      }
    }

    List<StudioImage> imagesToDelete = existingImagesMap.values().stream()
        .filter(existing -> !requestedKeysSet.contains(existing.getImageKey()))
        .toList();

    studioImageRepository.deleteAll(imagesToDelete);
    studioImageRepository.saveAll(finalImages);
    imagesToDelete.forEach(
        image -> fileStorageService.softDelete(image.getImageKey(), FileStorageLocation.PUBLIC_PERMANENT));

    return finalImages;
  }

  private void updateOptions(Studio studio, List<String> optionCodes) {
    Map<String, StudioOption> existingOptionMap = studioOptionRepository.findAllByStudio(studio)
        .stream()
        .collect(Collectors.toMap(so -> so.getOption().getCode(), so -> so));

    Set<String> requestOptionCodes = (optionCodes == null) ? new HashSet<>()
        : new HashSet<>(optionCodes);
    Set<StudioOption> optionsToSave = new HashSet<>();

    for (String reqCode : requestOptionCodes) {
      if (!existingOptionMap.containsKey(reqCode)) {
        optionRepository.findByCode(reqCode).ifPresent(option ->
            optionsToSave.add(StudioOption.builder().studio(studio).option(option).build())
        );
      }
    }

    List<StudioOption> optionsToDelete = existingOptionMap.values().stream()
        .filter(existing -> !requestOptionCodes.contains(existing.getOption().getCode()))
        .toList();

    studioOptionRepository.saveAll(optionsToSave);
    studioOptionRepository.deleteAll(optionsToDelete);
  }

  private void updateForbiddenInstruments(Studio studio, List<String> instrumentCodes) {
    Map<String, StudioForbiddenInstrument> existingInstrumentMap = studioForbiddenInstrumentRepository.findAllByStudio(
            studio)
        .stream()
        .collect(Collectors.toMap(sfi -> sfi.getInstrument().getCode(), sfi -> sfi));

    Set<String> requestInstrumentCodes = (instrumentCodes == null) ? new HashSet<>()
        : new HashSet<>(instrumentCodes);
    Set<StudioForbiddenInstrument> instrumentsToSave = new HashSet<>();

    for (String reqCode : requestInstrumentCodes) {
      if (!existingInstrumentMap.containsKey(reqCode)) {
        instrumentRepository.findByCode(reqCode).ifPresent(instrument ->
            instrumentsToSave.add(
                StudioForbiddenInstrument.builder().studio(studio).instrument(instrument).build())
        );
      }
    }

    List<StudioForbiddenInstrument> instrumentsToDelete = existingInstrumentMap.values().stream()
        .filter(
            existing -> !requestInstrumentCodes.contains(existing.getInstrument().getCode()))
        .toList();

    studioForbiddenInstrumentRepository.saveAll(instrumentsToSave);
    studioForbiddenInstrumentRepository.deleteAll(instrumentsToDelete);
  }

  private void updateNearbyStations(Studio studio, List<NearbyStationRequest> nearbyStationRequests) {
    Map<Long, SubwayStationNearbyStudio> existingStationMap = subwayStationNearbyStudioRepository.findAllByStudioIdOrderBySequenceAsc(
            studio.getId())
        .stream()
        .collect(Collectors.toMap(sns -> sns.getSubwayStation().getId(), sns -> sns));

    List<SubwayStationNearbyStudio> stationsToSave = new ArrayList<>();
    Set<Long> processedStationIds = new HashSet<>();

    if (nearbyStationRequests != null) {
      for (NearbyStationRequest stationReq : nearbyStationRequests) {
        Long stationId = stationReq.subwayStationId();
        processedStationIds.add(stationId);
        SubwayStationNearbyStudio existingStation = existingStationMap.get(stationId);

        if (existingStation != null) {
          existingStation.updateSequence(stationReq.sequence());
          stationsToSave.add(existingStation);
        } else {
          SubwayStation station = subwayStationRepository.findById(stationId)
              .orElseThrow(() -> new BusinessException(SubwayErrorCode.SUBWAY_NOT_FOUND));
          stationsToSave.add(SubwayStationNearbyStudio.builder()
              .studioId(studio.getId())
              .subwayStation(station)
              .sequence(stationReq.sequence())
              .build());
        }
      }
    }

    List<SubwayStationNearbyStudio> stationsToDelete = existingStationMap.values().stream()
        .filter(existing -> !processedStationIds.contains(existing.getSubwayStation().getId()))
        .toList();

    subwayStationNearbyStudioRepository.saveAll(stationsToSave);
    subwayStationNearbyStudioRepository.deleteAll(stationsToDelete);
  }

  public void deleteStudio(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new BusinessException(StudioErrorCode.STUDIO_NOT_FOUND));

    List<String> imageKeys = studioImageRepository.findAllByStudio(studio).stream()
        .map(StudioImage::getImageKey)
        .toList();
    imageKeys.forEach(imageKey -> fileStorageService.softDelete(imageKey, FileStorageLocation.PUBLIC_PERMANENT));

    roomRepository.deleteAllByStudioId(studioId);
    studioPriceRepository.deleteById(studioId);
    studioBuildingInfoRepository.deleteById(studioId);
    studioOptionRepository.deleteAllByStudio(studio);
    studioForbiddenInstrumentRepository.deleteAllByStudio(studio);
    studioImageRepository.deleteAllByStudio(studio);
    subwayStationNearbyStudioRepository.deleteAllByStudioId(studioId);
    studioRepository.delete(studio);
  }

  private StudioBuildingInfo buildStudioBuildingInfo(BuildingInfoRequest request) {
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
            .studioId(studioId)
            .name(roomReq.roomName())
            .basePrice(roomReq.basePrice())
            .isAvailable(roomReq.isAvailable())
            .availableAt(roomReq.availableAt())
            .width(roomReq.widthMm())
            .height(roomReq.heightMm())
            .sequence(sequence.getAndIncrement())
            .build())
        .toList();
  }

  private Set<StudioOption> buildStudioOptions(List<String> optionCodes, Studio studio) {
    if (optionCodes == null) {
      return new HashSet<>();
    }
    return optionRepository.findAllByCodeIn(optionCodes).stream()
        .map(option -> StudioOption.builder().studio(studio).option(option).build())
        .collect(Collectors.toSet());
  }

  private Set<StudioForbiddenInstrument> buildForbiddenInstruments(List<String> instrumentCodes,
      Studio studio) {
    if (instrumentCodes == null) {
      return new HashSet<>();
    }
    return instrumentRepository.findAllByCodeIn(instrumentCodes).stream()
        .map(instrument -> StudioForbiddenInstrument.builder().studio(studio)
            .instrument(instrument).build())
        .collect(Collectors.toSet());
  }

  private List<StudioImage> buildStudioImagesForCreate(ImageKeysRequest request) {
    if (request == null) {
      return new ArrayList<>();
    }
    List<StudioImage> images = new ArrayList<>();

    AtomicInteger sequence = new AtomicInteger(1);
    request.mainImageKeys()
        .forEach(key -> images.add(StudioImage.builder()
            .category(StudioImageCategory.MAIN)
            .imageKey(fileStorageService.move(key, FileStorageLocation.PUBLIC_TEMP, FileStorageLocation.PUBLIC_PERMANENT))
            .sequence(sequence.getAndIncrement())
            .build()));

    sequence.set(1);
    if (request.buildingImageKeys() != null) {
      request.buildingImageKeys()
          .forEach(key -> images.add(StudioImage.builder()
              .category(StudioImageCategory.BUILDING)
              .imageKey(fileStorageService.move(key, FileStorageLocation.PUBLIC_TEMP, FileStorageLocation.PUBLIC_PERMANENT))
              .sequence(sequence.getAndIncrement())
              .build()));
    }

    sequence.set(1);
    if (request.roomImageKeys() != null) {
      request.roomImageKeys()
          .forEach(key -> images.add(StudioImage.builder()
              .category(StudioImageCategory.ROOM)
              .imageKey(fileStorageService.move(key, FileStorageLocation.PUBLIC_TEMP, FileStorageLocation.PUBLIC_PERMANENT))
              .sequence(sequence.getAndIncrement())
              .build()));
    }

    images.add(StudioImage.builder()
        .category(StudioImageCategory.BLUEPRINT)
        .imageKey(
            fileStorageService.move(request.blueprintImageKey(), FileStorageLocation.PUBLIC_TEMP, FileStorageLocation.PUBLIC_PERMANENT))
        .sequence(1)
        .build());

    sequence.set(1);
    if (request.commonOptionImageKeys() != null) {
      request.commonOptionImageKeys()
          .forEach(key -> images.add(StudioImage.builder()
              .category(StudioImageCategory.COMMON_OPTION)
              .imageKey(fileStorageService.move(key, FileStorageLocation.PUBLIC_TEMP, FileStorageLocation.PUBLIC_PERMANENT))
              .sequence(sequence.getAndIncrement())
              .build()));
    }

    sequence.set(1);
    if (request.individualOptionImageKeys() != null) {
      request.individualOptionImageKeys()
          .forEach(key -> images.add(StudioImage.builder()
              .category(StudioImageCategory.INDIVIDUAL_OPTION)
              .imageKey(fileStorageService.move(key, FileStorageLocation.PUBLIC_TEMP, FileStorageLocation.PUBLIC_PERMANENT))
              .sequence(sequence.getAndIncrement())
              .build()));
    }

    return images;
  }

  private List<SubwayStationNearbyStudio> buildNearbyStations(List<NearbyStationRequest> request,
      Long studioId) {
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
