package kr.muroom.muroombackendbach.studio.domain.valueobject;

import lombok.Builder;

@Builder
public record NearbyStationInfo(
    Long subwayStationId,
    Integer sequence
) {}
