package kr.muroom.muroombackendbach.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파일 업로드 타입을 나타내는 열거형입니다.
 *
 * <p>S3 버킷 내의 도메인(폴더) 구분을 위해 사용됩니다.
 */
@Getter
@RequiredArgsConstructor
public enum FileUploadType {
  BETA_PROPERTY("beta-property"),
  STUDIO("studio"),
  ;

  private final String domain;
}
