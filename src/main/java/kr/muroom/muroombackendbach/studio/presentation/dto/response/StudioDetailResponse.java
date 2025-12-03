package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import kr.muroom.muroombackendbach.room.domain.entity.Room;
import kr.muroom.muroombackendbach.studio.domain.entity.Option;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioBuildingInfo;
import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.domain.enums.ParkingFeeType;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayStationInfo;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import kr.muroom.muroombackendbach.user.domain.entity.UserStatus;
import lombok.Builder;

@Builder
@Schema(description = "작업실 상세 내용")
public record StudioDetailResponse(
    @Schema(description = "스튜디오 기본 정보")
    StudioBaseInfoDto studioBaseInfo,

    @Schema(description = "스튜디오 건물 정보")
    StudioBuildingInfoDto studioBuildingInfo,

    @Schema(description = "스튜디오 안내사항")
    StudioNoticeDto studioNotice,

    @Schema(description = "스튜디오 금지 악기 정보")
    StudioForbiddenInstrumentsDto studioForbiddenInstruments,

    @Schema(description = "스튜디오 방 정보")
    StudioRoomsDto studioRooms,

    @Schema(description = "스튜디오 옵션 정보")
    StudioOptionsDto studioOptions
) {

  @Builder
  public record StudioBaseInfoDto(
      @Schema(description = "스튜디오 ID", example = "1")
      Long studioId,

      @Schema(description = "스튜디오 이름", example = "뮤룸 스튜디오")
      String studioName,

      @Schema(description = "스튜디오 주소", example = "서울특별시 강남구 테헤란로 427 위워크타워 5층")
      String address,

      @Schema(description = "스튜디오 경도", example = "127.027610")
      Double studioLongitude,

      @Schema(description = "스튜디오 위도", example = "37.499122")
      Double studioLatitude,

      @Schema(description = "스튜디오 최저 가격", example = "150000")
      Integer studioMinPrice,

      @Schema(description = "스튜디오 최고 가격", example = "430000")
      Integer studioMaxPrice,

      @Schema(description = "보증금 금액", example = "50000")
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
              """)
      List<StudioSubwayStationInfo> nearbySubwayStations,

      @Schema(description = "스튜디오 메인 이미지 URL 목록", example = "[\"https://example.com/studio_main1.jpg\", \"https://example"
          + ".com/studio_main2.jpg\"]")
      List<String> studioMainImageUrls
  ) {

  }

  @Builder
  public record StudioBuildingInfoDto(
      @Schema(description = "층 유형", example = "GROUND")
      FloorType floorType,

      @Schema(description = "층 번호", example = "5")
      Integer floorNumber,

      @Schema(description = "주차 가능 여부", example = "true")
      Boolean isParkingAvailable,

      @Schema(description = "주차 요금 유형", example = "FREE")
      ParkingFeeType parkingFeeType,

      @Schema(description = "주차 요금 정보", example = "매월 3만원")
      String parkingFeeInfo,

      @Schema(description = "주차 가능 대수", example = "6")
      Integer parkingSpots,

      @Schema(description = "주차 위치 이름", example = "위워크타워 지하 주차장")
      String parkingLocationName,

      @Schema(description = "주차 위치 주소", example = "서울특별시 강남구 테헤란로 427 위워크타워 지하 1층")
      String parkingLocationAddress,

      @Schema(description = "숙식 가능 여부", example = "true")
      Boolean isLodgingAvailable,

      @Schema(description = "화재 보험 가입 여부", example = "true")
      Boolean hasFireInsurance,

      @Schema(description = "스튜디오 건물 이미지 URL 목록",
          example = "[\"https://example.com/building1.jpg\", \"https://example.com/building2.jpg\"]")
      List<String> studioBuildingImageUrls
  ) {

    public static StudioBuildingInfoDto from(StudioBuildingInfo studioBuildingInfo, List<String> studioBuildingImageUrls) {
      return StudioBuildingInfoDto.builder()
          .floorType(studioBuildingInfo.getFloorType())
          .floorNumber(studioBuildingInfo.getFloorNumber())
          .isParkingAvailable(studioBuildingInfo.getIsParkingAvailable())
          .parkingFeeType(studioBuildingInfo.getParkingFeeType())
          .parkingFeeInfo(studioBuildingInfo.getParkingFeeInfo())
          .parkingSpots(studioBuildingInfo.getParkingSpots())
          .parkingLocationName(studioBuildingInfo.getParkingLocationName())
          .parkingLocationAddress(studioBuildingInfo.getParkingLocationAddress())
          .isLodgingAvailable(studioBuildingInfo.getIsLodgingAvailable())
          .hasFireInsurance(studioBuildingInfo.getHasFireInsurance())
          .studioBuildingImageUrls(studioBuildingImageUrls)
          .build();
    }
  }

  @Builder
  public record StudioNoticeDto(
      @Schema(description = "소유자 닉네임", example = "뮤루뮤루")
      String ownerNickname,

      @Schema(description = "소유자 전화번호", example = "010-1234-5678")
      String ownerPhoneNumber,

      @Schema(description = "운영 경력(년)", example = "5")
      Integer experienceYears,

      @Schema(description = "본인인증 여부", example = "false")
      Boolean isIdentityVerified,

      @Schema(description = "소개글", example = "안녕하세요! 뮤룸 스튜디오입니다.\n저희 스튜디오는 쾌적한 환경과 최신 장비를 갖추고 있어 여러분의 창작 활동을 지원합니다.\n많은 관심 부탁드립니다!")
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
      @Schema(description = "금지 악기 목록", example = "[\"드럼\", \"금관\"]")
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
      @Schema(description = "방 이미지 URL 목록", example = "[\"https://example.com/room1.jpg\", \"https://example.com/room2.jpg\"]")
      List<String> roomImageUrls,

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
              """)
      List<RoomInfoDto> rooms
  ) {

    public static StudioRoomsDto from(Set<Room> rooms, List<String> roomImageUrls) {
      return StudioRoomsDto.builder()
          .roomImageUrls(roomImageUrls)
          .rooms(rooms.stream().map(RoomInfoDto::from).toList())
          .build();
    }
  }

  @Builder
  public record RoomInfoDto(
      @Schema(description = "방 ID", example = "132")
      Long roomId,

      @Schema(description = "방 이름", example = "Room A")
      String roomName,

      @Schema(description = "입주 가능 여부", example = "true")
      Boolean isAvailable,

      @Schema(description = "입주 가능 일자", example = "2024-11-15")
      LocalDate availableAt,

      @Schema(description = "방 가로 길이 (mm)", example = "5000")
      Integer widthMm,

      @Schema(description = "방 세로 길이 (mm)", example = "4000")
      Integer heightMm,

      @Schema(description = "방 기본 가격 (원)", example = "250000")
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
                  "iconImageKey": "https://example.com/icons/water_purifier.png"
                },
                {
                  "code": "AIR_CONDITIONER",
                  "description": "에어컨",
                  "iconImageKey": "https://example.com/icons/air_conditioner.png"
                }
              ]
              """)
      List<OptionDto> commonOptions,

      @Schema(description = "개별 옵션 목록", implementation = OptionDto.class,
          example = """
              [
                {
                  "code": "AIR_CONDITIONER",
                  "description": "에어컨",
                  "iconImageKey": "https://example.com/icons/air_conditioner.png"
                },
                {
                  "code": "WINDOW",
                  "description": "창문",
                  "iconImageKey": "https://example.com/icons/window.png"
                }
              ]
              """)
      List<OptionDto> individualOptions
  ) {

  }

  @Builder
  public record OptionDto(
      @Schema(description = "옵션 코드", example = "WATER_PURIFIER")
      String code,

      @Schema(description = "옵션 설명", example = "정수기")
      String description,

      @Schema(description = "옵션 아이콘 이미지 URL", example = "https://example.com/icons/water_purifier.png")
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
}
