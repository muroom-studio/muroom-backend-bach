package kr.muroom.muroombackendbach.terms.presentation.dto;

import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;

import java.time.OffsetDateTime;

public final class TermDto {

  private TermDto() {
  }

  public record TermsWithContentDto(
      Long termId,
      TermsType type,
      String targetRole,
      String version,
      boolean isMandatory,
      OffsetDateTime effectiveAt
  ) {

  }

  public record TermContentDto(
      Long termId,
      String content
  ) {

  }

}

