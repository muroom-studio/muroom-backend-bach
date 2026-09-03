package kr.muroom.muroombackendbach.admin.studio.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest.ImageKeysRequest;
import kr.muroom.muroombackendbach.admin.studio.presentation.dto.request.StudioCreateRequest.RoomInfoRequest;

public record StudioUpdateRequest(
    @NotBlank
    @Schema(description = "스튜디오 이름")
    String studioName,

    @NotNull
    @Schema(description = "주소 정보")
    StudioCreateRequest.AddressInfoRequest addressInfo,

    @Schema(description = "스튜디오 최소 가격")
    Integer studioMinPrice,

    @Schema(description = "스튜디오 최대 가격")
    Integer studioMaxPrice,

    @Schema(description = "보증금")
    Integer depositAmount,

    @Size(min = 1, max = 3)
    @Schema(description = "인근 지하철역 목록")
    List<StudioCreateRequest.NearbyStationRequest> nearbyStations,

    @Schema(description = "소개글")
    String introduction,

    @NotNull
    @Schema(description = "소유주 전화번호")
    String ownerPhoneNumber,

    @NotNull
    @Schema(description = "건물 정보")
    StudioCreateRequest.BuildingInfoRequest buildingInfo,

    @Schema(description = "옵션 코드 목록")
    List<String> optionCodes,

    @Schema(description = "금지 악기 코드 목록")
    List<String> forbiddenInstrumentCodes,

    @Schema(description = "룸 정보 목록")
    List<RoomInfoRequest> rooms,

    @Schema(description = "이미지 키 목록")
    ImageKeysRequest imageKeys
) {

}