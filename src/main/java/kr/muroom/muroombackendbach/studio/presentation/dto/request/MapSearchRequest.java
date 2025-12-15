package kr.muroom.muroombackendbach.studio.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomGender;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomLocation;
import lombok.Builder;
import org.springframework.util.CollectionUtils;

@Builder
public record MapSearchRequest(
    @Schema(description = "검색어 (스튜디오명 또는 지하철역명)", example = "강남")
    String keyword,

    @Schema(description = "최소 위도", example = "37.4", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    Double minLatitude,

    @Schema(description = "최대 위도", example = "37.6", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    Double maxLatitude,

    @Schema(description = "최소 경도", example = "126.9", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    Double minLongitude,

    @Schema(description = "최대 경도", example = "127.2", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    Double maxLongitude,

    @Schema(description = "필터링할 공용 시설 옵션 코드 목록. 'ALL'을 전달하면 모든 공용 옵션을 가진 스튜디오를 조회합니다.",
        example = "[\"WIFI\", \"CCTV\"]")
    Set<String> commonOptionCodes,

    @Schema(description = "필터링할 개인 시설 옵션 코드 목록. 'ALL'을 전달하면 모든 개인 옵션을 가진 스튜디오를 조회합니다.",
        example = "[\"AIR_CONDITIONER\"]")
    Set<String> individualOptionCodes,

    @Schema(description = "검색할 최소 가격 (단위: 원)", example = "250000")
    Integer minPrice,

    @Schema(description = "검색할 최대 가격 (단위: 원)", example = "700000")
    Integer maxPrice,

    @Schema(description = "검색할 최소 방 너비 (단위: mm)", example = "2500")
    Integer minRoomWidth,

    @Schema(description = "검색할 최대 방 너비 (단위: mm)", example = "5000")
    Integer maxRoomWidth,

    @Schema(description = "검색할 최소 방 높이 (단위: mm)", example = "2500")
    Integer minRoomHeight,

    @Schema(description = "검색할 최대 방 높이 (단위: mm)", example = "5500")
    Integer maxRoomHeight,

    @Schema(description = "층 유형 목록", example = "[\"GROUND\", \"BASEMENT\"]")
    Set<FloorType> floorTypes,

    @Schema(description = "화장실 유형 목록", example = "[\"INTERNAL\", \"EXTERNAL\", \"SEPARATE\", \"UNISEX\"]", nullable = true)
    Set<String> restroomTypes,

    @Schema(description = "화장실 위치 목록", hidden = true)
    Set<RestroomLocation> restroomLocations,

    @Schema(description = "화장실 성별 목록", hidden = true)
    Set<RestroomGender> restroomGenders,

    @Schema(description = "주차 가능 여부", example = "true")
    Boolean isParkingAvailable,

    @Schema(description = "숙박 가능 여부", example = "false")
    Boolean isLodgingAvailable,

    @Schema(description = "화재 보험 가입 여부", example = "true")
    Boolean hasFireInsurance,

    @Schema(description = "사용 불가능한 악기 코드 목록", example = "[\"BRASS_WIND" + "\"]")
    Set<String> forbiddenInstrumentCodes
) {

  @JsonCreator
  public MapSearchRequest(
      String keyword,
      @NotNull Double minLatitude,
      @NotNull Double maxLatitude,
      @NotNull Double minLongitude,
      @NotNull Double maxLongitude,
      Set<String> commonOptionCodes,
      Set<String> individualOptionCodes,
      Integer minPrice,
      Integer maxPrice,
      Integer minRoomWidth,
      Integer maxRoomWidth,
      Integer minRoomHeight,
      Integer maxRoomHeight,
      Set<FloorType> floorTypes,
      Set<String> restroomTypes,
      Set<RestroomLocation> restroomLocations,
      Set<RestroomGender> restroomGenders,
      Boolean isParkingAvailable,
      Boolean isLodgingAvailable,
      Boolean hasFireInsurance,
      Set<String> forbiddenInstrumentCodes
  ) {
    this.keyword = keyword;
    this.minLatitude = minLatitude;
    this.maxLatitude = maxLatitude;
    this.minLongitude = minLongitude;
    this.maxLongitude = maxLongitude;
    this.commonOptionCodes = commonOptionCodes;
    this.individualOptionCodes = individualOptionCodes;
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;
    this.minRoomWidth = minRoomWidth;
    this.maxRoomWidth = maxRoomWidth;
    this.minRoomHeight = minRoomHeight;
    this.maxRoomHeight = maxRoomHeight;
    this.floorTypes = floorTypes;
    this.restroomTypes = restroomTypes;

    Set<RestroomLocation> derivedLocations = new HashSet<>();
    Set<RestroomGender> derivedGenders = new HashSet<>();
    if (!CollectionUtils.isEmpty(restroomTypes)) {
      for (String restroomType : restroomTypes) {
        if (restroomType == null) {
          continue;
        }
        switch (restroomType.toUpperCase()) {
          case "INTERNAL" -> derivedLocations.add(RestroomLocation.INTERNAL);
          case "EXTERNAL" -> derivedLocations.add(RestroomLocation.EXTERNAL);
          case "SEPARATE" -> derivedGenders.add(RestroomGender.SEPARATE);
          case "UNISEX" -> derivedGenders.add(RestroomGender.UNISEX);
          default -> {
            // ignored
          }
        }
      }
    }

    this.restroomLocations = derivedLocations.isEmpty() ? null : derivedLocations;
    this.restroomGenders = derivedGenders.isEmpty() ? null : derivedGenders;
    this.isParkingAvailable = isParkingAvailable;
    this.isLodgingAvailable = isLodgingAvailable;
    this.hasFireInsurance = hasFireInsurance;
    this.forbiddenInstrumentCodes = forbiddenInstrumentCodes;
  }
}