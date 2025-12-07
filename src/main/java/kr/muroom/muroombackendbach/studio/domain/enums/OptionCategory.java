package kr.muroom.muroombackendbach.studio.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.muroom.muroombackendbach.common.domain.EnumMapperType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OptionCategory implements EnumMapperType {
  COMMON("공용"),
  INDIVIDUAL("개인"),
  ETC("기타"),
  ;

  private final String description;

  @Override
  public String getCode() {
    return name();
  }

}
