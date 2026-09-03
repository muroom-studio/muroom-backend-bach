package kr.muroom.muroombackendbach.inquiry.domain.entity;

import kr.muroom.muroombackendbach.common.domain.EnumMapperType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryStatus implements EnumMapperType {
  PROCESSING("처리중"),
  COMPLETED("완료");

  private final String description;

  @Override
  public String getCode() {
    return name();
  }

}
