package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import kr.muroom.muroombackendbach.room.domain.entity.Room;
import kr.muroom.muroombackendbach.studio.domain.entity.Option;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioBuildingInfo;
import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.domain.enums.ParkingFeeType;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomGender;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomLocation;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayStationInfo;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import kr.muroom.muroombackendbach.user.domain.entity.UserStatus;
import lombok.Builder;
import org.locationtech.jts.geom.Point;

@Builder
@Schema(description = "작업실 상세 내용")
public record StudioDetailResponse(
    @Schema(description = "스튜디오 기본 정보", requiredMode = RequiredMode.REQUIRED)
    StudioBaseInfoDto studioBaseInfo,

    @Schema(description = "스튜디오 건물 정보", requiredMode = RequiredMode.REQUIRED)
    StudioBuildingInfoDto studioBuildingInfo,

    @Schema(description = "스튜디오 안내사항", requiredMode = RequiredMode.REQUIRED)
    StudioNoticeDto studioNotice,

    @Schema(description = "스튜디오 금지 악기 정보", nullable = true)
    StudioForbiddenInstrumentsDto studioForbiddenInstruments,

    @Schema(description = "스튜디오 방 정보", nullable = true)
    StudioRoomsDto studioRooms,

    @Schema(description = "스튜디오 옵션 정보", nullable = true)
    StudioOptionsDto studioOptions,

    @Schema(description = "스튜디오 이미지 정보 [메인, 건물, 방, 도면, 공용 옵션, 개인 옵션]", requiredMode = RequiredMode.REQUIRED)
    StudioImagesDto studioImages
) {

  @Builder
  public record StudioBaseInfoDto(
      @Schema(description = "스튜디오 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
      Long studioId,

      @Schema(description = "스튜디오 이름", example = "뮤룸 스튜디오", requiredMode = Schema.RequiredMode.REQUIRED)
      String studioName,

      @Schema(description = "스튜디오 도로명 주소", example = "서울특별시 강남구 테헤란로 427 위워크타워 5층", requiredMode = Schema.RequiredMode.REQUIRED)
      String roadNameAddress,

      @Schema(description = "스튜디오 지번 주소", example = "서울특별시 강남구 역삼동 701-21 위워크타워 5층", requiredMode = Schema.RequiredMode.REQUIRED)
      String lotNumberAddress,

      @Schema(description = "스튜디오 상세 주소", example = "5층", requiredMode = Schema.RequiredMode.REQUIRED)
      String detailedAddress,

      @Schema(description = "스튜디오 경도", example = "127.027610", requiredMode = Schema.RequiredMode.REQUIRED)
      Double studioLongitude,

      @Schema(description = "스튜디오 위도", example = "37.499122", requiredMode = Schema.RequiredMode.REQUIRED)
      Double studioLatitude,

      @Schema(description = "스튜디오 최저 가격", example = "150000", nullable = true)
      Integer studioMinPrice,

      @Schema(description = "스튜디오 최고 가격", example = "430000", nullable = true)
      Integer studioMaxPrice,

      @Schema(description = "보증금 금액", example = "50000", nullable = true)
      Integer depositAmount,

      @Schema(description = "인근 지하철역 정보", implementation = StudioSubwayStationInfo.class,
          example = """
              [
                {
                  "stationName": "강남역",
                  "lines": [
                    {
                      "lineName": "2호선",
                      "lineColor": "#1DB446"
                    },
                    {
                      "lineName": "신분당선",
                      "lineColor": "#FF3300"
                    }
                  ],
                  "walkingTimeMinutes": 8
                },
                {
                  "stationName": "역삼역",
                  "lines": [
                    {
                      "lineName": "2호선",
                      "lineColor": "#1DB446"
                    }
                  ],
                  "walkingTimeMinutes": 12
                }
              ]
              """, defaultValue = "[]", requiredMode = Schema.RequiredMode.REQUIRED)
      List<StudioSubwayStationInfo> nearbySubwayStations
  ) {

  }

  @Builder
  public record StudioBuildingInfoDto(
      @Schema(description = "층 유형", example = "GROUND", requiredMode = Schema.RequiredMode.REQUIRED)
      FloorType floorType,

      @Schema(description = "층 번호", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
      Integer floorNumber,

      @Schema(description = "화장실 보유 여부 (true라면, restoomLocation, restroomGender 중 1개는 반드시 null이 아님",
          example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
      Boolean hasRestroom,

      @Schema(description = "화장실 위치 (hasRestroom이 true일 때만)", example = "SHARED_RESTROOM", nullable = true)
      RestroomLocation restroomLocation,

      @Schema(description = "화장실 성별 구분 (hasRestroom이 true일 때만)", example = "SEPARATE", nullable = true)
      RestroomGender restroomGender,

      @Schema(description = "주차 요금 유형 (FREE/PAID/NONE)", example = "FREE", nullable = true)
      ParkingFeeType parkingFeeType,

      @Schema(description = "주차 요금 정보", example = "매월 3만원", nullable = true)
      String parkingFeeInfo,

      @Schema(description = "주차 가능 대수", example = "6", nullable = true)
      Integer parkingSpots,

      @Schema(description = "주차 위치 이름", example = "위워크타워 지하 주차장", nullable = true)
      String parkingLocationName,

      @Schema(description = "주차 위치 주소", example = "서울특별시 강남구 테헤란로 427 위워크타워 지하 1층", nullable = true)
      String parkingLocationAddress,

      @Schema(description = "주차 위치 경도", example = "127.027500", nullable = true)
      Double parkingLocationLongitude,

      @Schema(description = "주차 위치 위도", example = "37.499000", nullable = true)
      Double parkingLocationLatitude,

      @Schema(description = "숙식 가능 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
      Boolean isLodgingAvailable,

      @Schema(description = "화재 보험 가입 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
      Boolean hasFireInsurance
  ) {

    public static StudioBuildingInfoDto from(StudioBuildingInfo studioBuildingInfo, Point parkingLocation) {

      return StudioBuildingInfoDto.builder()
          .floorType(studioBuildingInfo.getFloorType())
          .floorNumber(studioBuildingInfo.getFloorNumber())
          .hasRestroom(studioBuildingInfo.getHasRestroom())
          .restroomLocation(studioBuildingInfo.getRestroomLocation())
          .restroomGender(studioBuildingInfo.getRestroomGender())
          .parkingFeeType(studioBuildingInfo.getParkingFeeType())
          .parkingFeeInfo(studioBuildingInfo.getParkingFeeInfo())
          .parkingSpots(studioBuildingInfo.getParkingSpots())
          .parkingLocationName(studioBuildingInfo.getParkingLocationName())
          .parkingLocationAddress(studioBuildingInfo.getParkingLocationAddress())
          .parkingLocationLongitude(parkingLocation != null ? parkingLocation.getX() : null)
          .parkingLocationLatitude(parkingLocation != null ? parkingLocation.getY() : null)
          .isLodgingAvailable(studioBuildingInfo.getIsLodgingAvailable())
          .hasFireInsurance(studioBuildingInfo.getHasFireInsurance())
          .build();
    }
  }

  @Builder
  public record StudioNoticeDto(
      @Schema(description = "소유자 닉네임", example = "뮤루뮤루", requiredMode = Schema.RequiredMode.REQUIRED)
      String ownerNickname,

      @Schema(description = "소유자 전화번호", example = "01012345678", requiredMode = Schema.RequiredMode.REQUIRED)
      String ownerPhoneNumber,

      @Schema(description = "운영 경력(년)", example = "5", nullable = true)
      Integer experienceYears,

      @Schema(description = "본인인증 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
      Boolean isIdentityVerified,

      @Schema(description = "소개글",
          example = "안녕하세요! 뮤룸 스튜디오입니다.\n저희 스튜디오는 쾌적한 환경과 최신 장비를 갖추고 있어 여러분의 창작 활동을 지원합니다.\n많은 관심 부탁드립니다!",
          nullable = true)
      String introduction
  ) {

    public static StudioNoticeDto from(Owner owner, Studio studio) {
      return StudioNoticeDto.builder()
          .ownerNickname(owner.getNickname())
          .ownerPhoneNumber(owner.getPhoneNumber())
          .experienceYears(owner.getExperienceYears())
          .isIdentityVerified(owner.getStatus() != UserStatus.UNVERIFIED)
          .introduction(studio.getIntroduction())
          .build();
    }
  }

  @Builder
  public record StudioForbiddenInstrumentsDto(
      @Schema(description = "금지 악기 목록", example = "[\"드럼\", \"금관\"]", defaultValue = "[]")
      List<String> instruments
  ) {

    public static StudioForbiddenInstrumentsDto from(Studio studio) {
      return StudioForbiddenInstrumentsDto.builder()
          .instruments(studio.getForbiddenInstruments()
              .stream()
              .map(forbiddenInstrument -> forbiddenInstrument.getInstrument().getDescription())
              .toList())
          .build();
    }
  }

  @Builder
  public record StudioRoomsDto(

      @Schema(description = "방 정보 목록", implementation = RoomInfoDto.class,
          example = """
              [
                {
                  "roomId": 132,
                  "roomName": "Room A",
                  "isAvailable": true,
                  "availableAt": "2024-11-15",
                  "widthMm": 5000,
                  "heightMm": 4000,
                  "roomBasePrice": 250000
                },
                {
                  "roomId": 133,
                  "roomName": "Room B",
                  "isAvailable": false,
                  "availableAt": "2024-12-01",
                  "widthMm": 6000,
                  "heightMm": 4500,
                  "roomBasePrice": 300000
                }
              ]
              """, defaultValue = "[]", requiredMode = Schema.RequiredMode.REQUIRED)
      List<RoomInfoDto> rooms
  ) {

    public static StudioRoomsDto from(Set<Room> rooms) {
      return StudioRoomsDto.builder()
          .rooms(rooms.stream().map(RoomInfoDto::from).toList())
          .build();
    }
  }

  @Builder
  public record RoomInfoDto(
      @Schema(description = "방 ID", example = "132", requiredMode = Schema.RequiredMode.REQUIRED)
      Long roomId,

      @Schema(description = "방 이름", example = "Room A", requiredMode = Schema.RequiredMode.REQUIRED)
      String roomName,

      @Schema(description = "입주 가능 여부", example = "true", nullable = true)
      Boolean isAvailable,

      @Schema(description = "입주 가능 일자", example = "2024-11-15", nullable = true)
      LocalDate availableAt,

      @Schema(description = "방 가로 길이 (mm)", example = "5000", requiredMode = Schema.RequiredMode.REQUIRED)
      Integer widthMm,

      @Schema(description = "방 세로 길이 (mm)", example = "4000", requiredMode = Schema.RequiredMode.REQUIRED)
      Integer heightMm,

      @Schema(description = "방 기본 가격 (원)", example = "250000", nullable = true)
      Integer roomBasePrice
  ) {

    public static RoomInfoDto from(Room room) {
      return RoomInfoDto.builder()
          .roomId(room.getId())
          .roomName(room.getName())
          .isAvailable(room.getIsAvailable())
          .availableAt(room.getAvailableAt())
          .roomBasePrice(room.getBasePrice())
          .widthMm(room.getWidth())
          .heightMm(room.getHeight())
          .build();
    }
  }

  @Builder
  public record StudioOptionsDto(
      @Schema(description = "공통 옵션 목록", implementation = OptionDto.class,
          example = """
              [
                {
                  "code": "WATER_PURIFIER",
                  "description": "정수기",
                  "iconImageKey": "
                },
                {
                  "code": "AIR_CONDITIONER",
                  "description": "에어컨",
                  "iconImageKey": "
                }
              ]
              """, defaultValue = "[]", requiredMode = Schema.RequiredMode.REQUIRED)
      List<OptionDto> commonOptions,

      @Schema(description = "개별 옵션 목록", implementation = OptionDto.class,
          example = """
              [
                {
                  "code": "AIR_CONDITIONER",
                  "description": "에어컨",
                  "iconImageKey": "
                },
                {
                  "code": "WINDOW",
                  "description": "창문",
                  "iconImageKey": "
                }
              ]
              """, defaultValue = "[]", requiredMode = Schema.RequiredMode.REQUIRED)
      List<OptionDto> individualOptions
  ) {

  }

  @Builder
  public record OptionDto(
      @Schema(description = "옵션 코드", example = "WATER_PURIFIER", requiredMode = RequiredMode.REQUIRED)
      String code,

      @Schema(description = "옵션 설명", example = "정수기", requiredMode = RequiredMode.REQUIRED)
      String description,

      @Schema(description = "옵션 아이콘 이미지 URL", example = "/systems/icons/WATER_PURIFIER.svg", requiredMode = RequiredMode.REQUIRED)
      String iconImageKey
  ) {

    public static OptionDto from(Option option) {
      return OptionDto.builder()
          .code(option.getCode())
          .description(option.getDescription())
          .iconImageKey(option.getIconImageKey())
          .build();
    }
  }

  @Builder
  public record StudioImagesDto(
      @Schema(description = "스튜디오 메인 이미지 URL 목록",
          example = "[\"/studios/main/image1.jpg\", \"/studios/main/image2.jpg\"]",
          requiredMode = RequiredMode.REQUIRED, minLength = 1)
      List<String> mainImageKeys,

      @Schema(description = "건물 이미지 URL 목록",
          example = "[\"/studios/building/image1.jpg\", \"/studios/building/image2.jpg\"]",
          defaultValue = "[]")
      List<String> buildingImageKeys,

      @Schema(description = "방 이미지 URL 목록",
          example = "[\"/studios/room/image1.jpg\", \"/studios/room/image2.jpg\"]",
          defaultValue = "[]")
      List<String> roomImageKeys,

      @Schema(description = "도면 이미지 URL",
          example = "/studios/blueprint/blueprint_image.jpg", nullable = true)
      String blueprintImageKey,

      @Schema(description = "공용 옵션(시설) 이미지 URL 목록",
          example = "[\"/studios/common_options/image1.jpg\", \"/studios/common_options/image2.jpg\"]",
          defaultValue = "[]")
      List<String> commonOptionImageKeys,

      @Schema(description = "개인 옵션(시설) 이미지 URL 목록",
          example = "[\"/studios/individual_options/image1.jpg\", \"/studios/individual_options/image2.jpg\"]",
          defaultValue = "[]")
      List<String> individualOptionImageKeys) {

  }
}
