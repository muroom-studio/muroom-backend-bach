package kr.muroom.muroombackendbach.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileUploadType {
  BETA_PROPERTY("beta-property"),
  ;

  private final String domain;
}
