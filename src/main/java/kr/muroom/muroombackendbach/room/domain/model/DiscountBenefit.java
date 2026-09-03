package kr.muroom.muroombackendbach.room.domain.model;

import kr.muroom.muroombackendbach.room.domain.enums.DiscountType;
import lombok.Builder;

@Builder
public record DiscountBenefit(
    String condition,
    Integer durationMonths,
    DiscountType discountType,
    Integer discountValue
) {
}
