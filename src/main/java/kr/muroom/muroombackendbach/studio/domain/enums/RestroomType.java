package kr.muroom.muroombackendbach.studio.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum RestroomType implements BuildingType {
  INTERNAL("내부"),
  EXTERNAL("외부"),
  SHARED("공용"),
  PRIVATE("단독");

  private final String description;

  @Override
  public String getCode() {
    return name();
  }
}