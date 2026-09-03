package kr.muroom.muroombackendbach.studio.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import kr.muroom.muroombackendbach.room.domain.enums.DiscountType;
import kr.muroom.muroombackendbach.room.domain.model.DiscountBenefit;
import kr.muroom.muroombackendbach.room.domain.valueobject.RoomInfo;
import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.domain.enums.ParkingFeeType;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomGender;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomLocation;
import kr.muroom.muroombackendbach.studio.domain.valueobject.AddressInfo;
import kr.muroom.muroombackendbach.studio.domain.valueobject.BuildingInfo;
import kr.muroom.muroombackendbach.studio.domain.valueobject.NearbyStationInfo;
import kr.muroom.muroombackendbach.studio.domain.valueobject.StudioDraftData;
import lombok.Builder;

@Schema(description = "스튜디오 임시 저장 요청")
public record StudioDraftSaveRequest(
    @Schema(description = "스튜디오 임시 저장 단계 (1~8단계)", example = "1", requiredMode = RequiredMode.REQUIRED)
    @NotNull @Min(1) @Max(8)
    Integer step,

    @Schema(description = "스튜디오 임시 저장 상세 데이터")
    @Valid
    StudioDraftDataRequest studioDraftData
) {

  @Builder
  @Schema(description = "스튜디오 임시 저장 데이터 본체")
  public record StudioDraftDataRequest(
      @Schema(description = "스튜디오 이름", example = "뮤룸 홍대점")
      String studioName,

      @Schema(description = "주소 정보")
      AddressInfoRequest addressInfo,

      @Schema(description = "인근 지하철역 정보 목록")
      List<NearbyStationRequest> nearbyStationInfos,

      @Schema(description = "메인 이미지 키 목록 (최대 3장)")
      @Size(max = 3)
      List<String> mainImageKeys,

      @Schema(description = "건물 이미지 키 목록 (최대 2장)")
      @Size(max = 2)
      List<String> buildingImageKeys,

      @Schema(description = "방 이미지 키 목록 (최대 20장)")
      @Size(max = 20)
      List<String> roomImageKeys,

      @Schema(description = "건물 상세 정보")
      BuildingInfoRequest buildingInfo,

      @Schema(description = "금지 악기 코드 목록", example = "[\"DRUM\", \"BRASS\"]")
      List<String> forbiddenInstrumentCodes,

      @Schema(description = "도면 이미지 키 (최대 1장)")
      String blueprintImageKey,

      @Schema(description = "가격 공개 여부", example = "true")
      boolean isPriceOpen,

      @Schema(description = "방 상세 정보 목록")
      List<RoomInfoRequest> roomInfos,

      @Schema(description = "옵션 코드 목록 (공통/개별 포함)")
      List<String> optionCodes,

      @Schema(description = "공통 옵션 이미지 키 목록 (최대 10장)")
      @Size(max = 10)
      List<String> commonOptionImageKeys,

      @Schema(description = "개별 옵션 이미지 키 목록 (최대 10장)")
      @Size(max = 10)
      List<String> individualOptionImageKeys,

      @Schema(description = "사장님 안내 사항 (최대 1500자)", example = "24시간 운영되는 쾌적한 연습실입니다.")
      @Size(max = 1500)
      String introduction
  ) {

    public StudioDraftData toDomain() {
      return StudioDraftData.builder()
          .studioName(this.studioName)
          .addressInfo(this.addressInfo != null ? this.addressInfo.toDomain() : null)
          .nearbyStationInfos(
              this.nearbyStationInfos != null ? this.nearbyStationInfos.stream().map(NearbyStationRequest::toDomain).toList() : null)
          .mainImageKeys(this.mainImageKeys)
          .buildingImageKeys(this.buildingImageKeys)
          .roomImageKeys(this.roomImageKeys)
          .buildingInfo(this.buildingInfo != null ? this.buildingInfo.toDomain() : null)
          .forbiddenInstrumentCodes(this.forbiddenInstrumentCodes)
          .blueprintImageKey(this.blueprintImageKey)
          .isPriceOpen(this.isPriceOpen)
          .roomInfos(this.roomInfos != null ? this.roomInfos.stream().map(RoomInfoRequest::toDomain).toList() : null)
          .optionCodes(this.optionCodes)
          .commonOptionImageKeys(this.commonOptionImageKeys)
          .individualOptionImageKeys(this.individualOptionImageKeys)
          .introduction(this.introduction)
          .build();
    }
  }

  @Schema(description = "주소 정보")
  public record AddressInfoRequest(
      @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 427") String roadNameAddress,
      @Schema(description = "지번 주소", example = "서울특별시 강남구 삼성동 143-37") String lotNumberAddress,
      @Schema(description = "상세 주소", example = "위워크타워 5층") String detailedAddress
  ) {

    public AddressInfo toDomain() {
      return AddressInfo.builder()
          .roadNameAddress(this.roadNameAddress)
          .lotNumberAddress(this.lotNumberAddress)
          .detailedAddress(this.detailedAddress)
          .build();
    }
  }

  @Schema(description = "인근 지하철역 정보")
  public record NearbyStationRequest(
      @Schema(description = "지하철역 ID", example = "1002") Long subwayStationId,
      @Schema(description = "순서", example = "1") Integer sequence
  ) {

    public NearbyStationInfo toDomain() {
      return NearbyStationInfo.builder()
          .subwayStationId(this.subwayStationId)
          .sequence(this.sequence)
          .build();
    }
  }

  @Schema(description = "건물 상세 정보")
  public record BuildingInfoRequest(
      @Schema(description = "층 유형", example = "GROUND") FloorType floorType,
      @Schema(description = "층 번호", example = "5") Integer floorNumber,
      @Schema(description = "화장실 보유 여부", example = "true") Boolean hasRestroom,
      @Schema(description = "화장실 성별", example = "SEPARATE") RestroomGender restroomGender,
      @Schema(description = "화장실 위치", example = "INSIDE") RestroomLocation restroomLocation,
      @Schema(description = "주차 요금 유형", example = "FREE") ParkingFeeType parkingFeeType,
      @Schema(description = "주차 요금 정보", example = "월 3만원") String parkingFeeInfo,
      @Schema(description = "주차 가능 대수", example = "10") Integer parkingSpots,
      @Schema(description = "주차 위치 명칭", example = "건물 지하 주차장") String parkingLocationName,
      @Schema(description = "주차 위치 주소", example = "서울특별시 강남구 테헤란로 427 지하 1층") String parkingLocationAddress,
      @Schema(description = "숙식 가능 여부", example = "false") Boolean isLodgingAvailable,
      @Schema(description = "화재 보험 가입 여부", example = "true") Boolean hasFireInsurance
  ) {

    public BuildingInfo toDomain() {
      return BuildingInfo.builder()
          .floorType(this.floorType)
          .floorNumber(this.floorNumber)
          .hasRestroom(this.hasRestroom)
          .restroomGender(this.restroomGender)
          .restroomLocation(this.restroomLocation)
          .parkingFeeType(this.parkingFeeType)
          .parkingFeeInfo(this.parkingFeeInfo)
          .parkingSpots(this.parkingSpots)
          .parkingLocationName(this.parkingLocationName)
          .parkingLocationAddress(this.parkingLocationAddress)
          .isLodgingAvailable(this.isLodgingAvailable)
          .hasFireInsurance(this.hasFireInsurance)
          .build();
    }
  }

  @Schema(description = "방 상세 정보")
  public record RoomInfoRequest(
      @Schema(description = "방 이름", example = "Room A") String roomName,
      @Schema(description = "입주 가능 여부 (true: 공실, false: 계약완료)", example = "true") Boolean isAvailable,
      @Schema(description = "입주 가능 일자", example = "2024-12-01") LocalDate availableAt,
      @Schema(description = "가로 길이(mm)", example = "3000") Integer widthMm,
      @Schema(description = "세로 길이(mm)", example = "4000") Integer heightMm,
      @Schema(description = "기본 가격", example = "500000") Integer basePrice,
      @Schema(description = "보증금", example = "1000000") Integer depositAmount,
      @Schema(description = "할인 혜택") DiscountBenefitRequest discountBenefit,
      @Schema(description = "추가 안내 사항") String additionalInfo
  ) {

    public RoomInfo toDomain() {
      return RoomInfo.builder()
          .roomName(this.roomName)
          .isAvailable(this.isAvailable)
          .availableAt(this.availableAt)
          .widthMm(this.widthMm)
          .heightMm(this.heightMm)
          .basePrice(this.basePrice)
          .depositAmount(this.depositAmount)
          .discountBenefit(this.discountBenefit != null ? this.discountBenefit.toDomain() : null)
          .additionalInfo(this.additionalInfo)
          .build();
    }
  }

  @Schema(description = "할인 혜택 정보")
  public record DiscountBenefitRequest(
      @Schema(description = "할인 조건", example = "6개월 이상 계약 시") String condition,
      @Schema(description = "할인 기간 (개월)", example = "3") Integer durationMonths,
      @Schema(description = "할인 유형", example = "PERCENTAGE") DiscountType discountType,
      @Schema(description = "할인 비중 및 금액", example = "10") Integer discountValue
  ) {

    public DiscountBenefit toDomain() {
      return DiscountBenefit.builder()
          .condition(this.condition)
          .durationMonths(this.durationMonths)
          .discountType(this.discountType)
          .discountValue(this.discountValue)
          .build();
    }
  }
}
