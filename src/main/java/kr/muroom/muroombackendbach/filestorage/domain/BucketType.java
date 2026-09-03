package kr.muroom.muroombackendbach.filestorage.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.muroom.muroombackendbach.common.domain.EnumMapperType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum BucketType implements EnumMapperType {
  PUBLIC("PUBLIC", "퍼블릿 버킷"),
  PRIVATE("PRIVATE", "프라이빗 버킷"),
  ;

  private final String code;
  private final String description;
}
