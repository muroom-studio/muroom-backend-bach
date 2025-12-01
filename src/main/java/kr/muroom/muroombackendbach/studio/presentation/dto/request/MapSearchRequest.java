package kr.muroom.muroombackendbach.studio.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomType;

public record MapSearchRequest(
    @Schema(description = "최소 위도", example = "37.4")
    Double minLatitude,

    @Schema(description = "최대 위도", example = "37.6")
    Double maxLatitude,

    @Schema(description = "최소 경도", example = "126.9")
    Double minLongitude,

    @Schema(description = "최대 경도", example = "127.2")
    Double maxLongitude,

    @Schema(description = "필터링할 공용 시설 옵션 코드 목록. 'ALL'을 전달하면 모든 공용 옵션을 가진 스튜디오를 조회합니다.",
        example = "[\"WIFI\", \"CCTV\"]")
    List<String> commonOptionCodes,

    @Schema(description = "필터링할 개인 시설 옵션 코드 목록. 'ALL'을 전달하면 모든 개인 옵션을 가진 스튜디오를 조회합니다.",
        example = "[\"AIR_CONDITIONER\"]")
    List<String> individualOptionCodes,

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
    List<FloorType> floorTypes,

    @Schema(description = "화장실 유형 목록",
        example = "[\"INTERNAL\", \"EXTERNAL\", \"PRIVATE\"]")
    List<RestroomType> restroomTypes,

    @Schema(description = "주차 가능 여부", example = "true")
    Boolean isParkingAvailable,

    @Schema(description = "숙박 가능 여부", example = "false")
    Boolean isLodgingAvailable,

    @Schema(description = "화재 보험 가입 여부", example = "true")
    Boolean hasFireInsurance,

    @Schema(description = "사용 불가능한 악기 코드 목록", example = "[\"BRASS_WIND\"]")
    List<String> forbiddenInstrumentCodes
) {

}