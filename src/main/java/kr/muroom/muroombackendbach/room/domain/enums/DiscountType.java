package kr.muroom.muroombackendbach.room.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.muroom.muroombackendbach.common.domain.EnumMapperType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DiscountType implements EnumMapperType {
  PERCENTAGE("PERCENTAGE", "퍼센트"),
  FIXED_AMOUNT("FIXED_AMOUNT", "고정 금액"),
  NONE("NONE", "할인 없음");

  private final String code;
  private final String description;
}
