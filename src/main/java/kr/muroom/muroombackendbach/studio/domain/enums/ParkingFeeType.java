package kr.muroom.muroombackendbach.studio.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ParkingFeeType implements BuildingType {
  FREE("무료"),
  PAID("유료"),
  NONE("제공 안함");

  private final String description;

  @Override
  public String getCode() {
    return name();
  }
}