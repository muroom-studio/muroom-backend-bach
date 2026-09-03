package kr.muroom.muroombackendbach.studio.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum RestroomGender implements BuildingType {
  SEPARATE("남녀구분"),
  UNISEX("남녀공용"),
  ;

  private final String description;

  @Override
  public String getCode() {
    return name();
  }
}