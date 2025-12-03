package kr.muroom.muroombackendbach.admin.studio.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.domain.enums.ParkingFeeType;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomType;

public record StudioCreateRequest(
    @NotBlank
    @Schema(description = "스튜디오 이름")
    String studioName,

    @NotNull
    @Schema(description = "주소 정보")
    AddressInfoRequest addressInfo,

    @Schema(description = "스튜디오 최소 가격")
    Integer studioMinPrice,

    @Schema(description = "스튜디오 최대 가격")
    Integer studioMaxPrice,

    @Schema(description = "보증금")
    Integer depositAmount,

    @Size(max = 3)
    @Schema(description = "인근 지하철역 목록")
    List<NearbyStationRequest> nearbyStations,

    @Schema(description = "소개글")
    String introduction,

    @NotNull
    @Schema(description = "소유주 전화번호")
    String ownerPhoneNumber,

    @NotNull
    @Schema(description = "건물 정보")
    BuildingInfoRequest buildingInfo,

    @Schema(description = "옵션 코드 목록")
    List<String> optionCodes,

    @Schema(description = "금지 악기 코드 목록")
    List<String> forbiddenInstrumentCodes,

    @Size(min = 1)
    @Schema(description = "룸 정보 목록")
    List<RoomInfoRequest> rooms,

    @Schema(description = "이미지 키 목록")
    ImageKeysRequest imageKeys
) {

  public record AddressInfoRequest(
      @NotBlank
      @Schema(description = "도로명 주소")
      String roadAddress,

      @NotBlank
      @Schema(description = "지번 주소")
      String jibunAddress,

      @NotBlank
      @Schema(description = "상세 주소")
      String detailedAddress,

      @NotBlank
      @Schema(description = "우편번호")
      String zipCode
  ) {

  }

  public record NearbyStationRequest(
      @NotNull
      @Schema(description = "지하철역 ID")
      Long subwayStationId,

      @NotNull
      @Schema(description = "순서 (숫자가 낮을수록 우선순위 높음)")
      Integer sequence
  ) {

  }

  public record BuildingInfoRequest(
      @NotNull FloorType floorType,
      @NotNull Integer floorNumber,

      @NotNull RestroomType restroomType,

      @NotNull Boolean isParkingAvailable,
      @NotNull ParkingFeeType parkingFeeType,
      String parkingFeeInfo,
      Integer parkingSpots,
      String parkingLocationName,
      String parkingLocationAddress,

      @NotNull Boolean isLodgingAvailable,
      @NotNull Boolean hasFireInsurance
  ) {

  }

  public record RoomInfoRequest(
      @NotBlank
      String roomName,

      Boolean isAvailable,

      @Schema(description = "입주 가능 날짜 (YYYY-MM-DD)")
      @JsonFormat(pattern = "yyyy-MM-dd")
      LocalDate availableAt,

      Integer widthMm,

      Integer heightMm,

      @NotNull
      Integer roomBasePrice
  ) {

  }

  public record ImageKeysRequest(
      @Size(min = 1, max = 3)
      List<String> mainImageKeys,

      @Size(max = 4)
      List<String> buildingImageKeys,

      @Size(max = 20)
      List<String> roomImageKeys,

      @NotBlank
      String blueprintImageKey
  ) {

  }
}