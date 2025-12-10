package kr.muroom.muroombackendbach.terms.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.muroom.muroombackendbach.common.domain.EnumMapperType;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TermsType implements EnumMapperType {
  TERMS_OF_USE("이용약관", true),
  PRIVACY_COLLECTION("개인정보 수집 및 이용", true),
  PRIVACY_PROCESSING("개인정보 처리방침", true),
  MARKETING_RECEIVE("마케팅 수신", false);

  private final String description;
  private final boolean required;

  TermsType(String description, boolean required) {
    this.description = description;
    this.required = required;
  }

  @Override
  public String getCode() {
    return name();
  }
}