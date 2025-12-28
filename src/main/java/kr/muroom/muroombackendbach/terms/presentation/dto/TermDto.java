package kr.muroom.muroombackendbach.terms.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import lombok.Builder;

public final class TermDto {

  private TermDto() {
  }

  @Schema(description = "약관 내용이 포함된 약관 정보")
  public record TermsWithContentDto(
      @Schema(description = "약관 ID", requiredMode = Schema.RequiredMode.REQUIRED)
      String termId,

      @Schema(description = "약관 종류", requiredMode = Schema.RequiredMode.REQUIRED)
      TermsType code,

      @Schema(description = "대상 역할", nullable = true)
      TargetRole targetRole,

      @Schema(example = "0.0.1", description = "약관 버전 3자리 관리", nullable = true)
      String version,

      @Schema(description = "약관 내용", requiredMode = Schema.RequiredMode.REQUIRED)
      Boolean isMandatory,

      @Schema(description = "약관 내용", nullable = true)
      OffsetDateTime effectiveAt
  ) {

  }

  @Builder
  public record TermContentDto(
      @Schema(description = "약관 ID", requiredMode = Schema.RequiredMode.REQUIRED)
      String termId,

      @Schema(description = "약관 제목", requiredMode = Schema.RequiredMode.REQUIRED)
      String title,

      @Schema(description = "약관 내용", requiredMode = Schema.RequiredMode.REQUIRED)
      String content
  ) {

  }

}

