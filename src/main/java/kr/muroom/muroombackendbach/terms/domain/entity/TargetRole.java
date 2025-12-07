package kr.muroom.muroombackendbach.terms.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.muroom.muroombackendbach.common.domain.EnumMapperType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@AllArgsConstructor
public enum TargetRole implements EnumMapperType {
  OWNER("사장님"),
  MUSICIAN("뮤지션");

  private final String description;

  @Override
  public String getCode() {
    return name();
  }
}
