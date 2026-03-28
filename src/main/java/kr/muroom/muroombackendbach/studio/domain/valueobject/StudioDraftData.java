package kr.muroom.muroombackendbach.studio.domain.valueobject;

import java.util.ArrayList;
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
) {

  public List<String> extractAllImageKeys() {
    List<String> keys = new ArrayList<>();
    if (mainImageKeys != null) {
      keys.addAll(mainImageKeys);
    }
    if (buildingImageKeys != null) {
      keys.addAll(buildingImageKeys);
    }
    if (roomImageKeys != null) {
      keys.addAll(roomImageKeys);
    }
    if (blueprintImageKey != null) {
      keys.add(blueprintImageKey);
    }
    if (commonOptionImageKeys != null) {
      keys.addAll(commonOptionImageKeys);
    }
    if (individualOptionImageKeys != null) {
      keys.addAll(individualOptionImageKeys);
    }
    return keys;
  }
}