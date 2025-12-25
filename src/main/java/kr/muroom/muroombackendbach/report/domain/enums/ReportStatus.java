package kr.muroom.muroombackendbach.report.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.muroom.muroombackendbach.common.domain.EnumMapperType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ReportStatus implements EnumMapperType {
  /**
   * 신고 접수됨 (아직 운영자가 확인하지 않음)
   */
  SUBMITTED("접수됨"),

  /**
   * 운영자가 확인하여 검토 중인 상태
   */
  IN_REVIEW("처리중"),

  /**
   * 신고 내용이 유효하여 조치 완료
   */
  RESOLVED("처리 완료"),

  /**
   * 신고 내용이 부적절하거나 사유 부족으로 반려됨
   */
  REJECTED("반려됨");

  private final String description;

  @Override
  public String getCode() {
    return name();
  }
}
