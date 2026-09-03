package kr.muroom.muroombackendbach.studio.domain.valueobject;

import lombok.Builder;

@Builder
public record AddressInfo(
    String roadNameAddress,
    String lotNumberAddress,
    String detailedAddress
) {}
