package kr.muroom.muroombackendbach.terms.domain.presentation.dto;

import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import lombok.Builder;

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
            OffsetDateTime effectiveAt,
            String content
    ) {}

}

