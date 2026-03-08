package kr.muroom.muroombackendbach.studio.domain.valueobject;

import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.domain.enums.ParkingFeeType;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomGender;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomLocation;
import lombok.Builder;

@Builder
public record BuildingInfo(
    FloorType floorType,
    Integer floorNumber,
    Boolean hasRestroom,
    RestroomGender restroomGender,
    RestroomLocation restroomLocation,
    ParkingFeeType parkingFeeType,
    String parkingFeeInfo,
    Integer parkingSpots,
    String parkingLocationName,
    String parkingLocationAddress,
    Boolean isLodgingAvailable,
    Boolean hasFireInsurance
) {}
