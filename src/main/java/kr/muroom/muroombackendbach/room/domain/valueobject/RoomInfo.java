package kr.muroom.muroombackendbach.room.domain.valueobject;

import java.time.LocalDate;
import kr.muroom.muroombackendbach.room.domain.model.DiscountBenefit;
import lombok.Builder;

@Builder
public record RoomInfo(
    String roomName,
    Boolean isAvailable,
    LocalDate availableAt,
    Integer widthMm,
    Integer heightMm,
    Integer basePrice,
    Integer depositAmount,
    DiscountBenefit discountBenefit,
    String additionalInfo
) {
}
