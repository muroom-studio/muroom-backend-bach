package kr.muroom.muroombackendbach.studio.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.muroom.muroombackendbach.common.domain.EnumMapperType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StudioImageCategory implements EnumMapperType {
  MAIN("대표"),
  BUILDING("건물"),
  ROOM("방"),
  BLUEPRINT("도면"),
  COMMON_OPTION("공용 옵션"),
  INDIVIDUAL_OPTION("개인 옵션");

  private final String description;

  @Override
  public String getCode() {
    return name();
  }
}
