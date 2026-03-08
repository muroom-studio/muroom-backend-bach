package kr.muroom.muroombackendbach.studio.domain.valueobject;

import java.util.List;
import kr.muroom.muroombackendbach.room.domain.valueobject.RoomInfo;
import lombok.Builder;

@Builder
public record StudioDraftData(
    String studioName,
    AddressInfo addressInfo,
    List<NearbyStationInfo> nearbyStationInfos,

    List<String> mainImageKeys,
    List<String> buildingImageKeys,
    List<String> roomImageKeys,

    BuildingInfo buildingInfo,

    List<String> forbiddenInstrumentCodes,

    String blueprintImageKey,

    boolean isPriceOpen,
    List<RoomInfo> roomInfos,

    List<String> optionCodes,
    List<String> commonOptionImageKeys,
    List<String> individualOptionImageKeys,

    String introduction
) {}