package kr.muroom.muroombackendbach.studio.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FloorType implements BuildingType {
  ALL("여러 층"),
  GROUND("지상"),
  BASEMENT("지하");

  private final String description;

  @Override
  public String getCode() {
    return name();
  }
}