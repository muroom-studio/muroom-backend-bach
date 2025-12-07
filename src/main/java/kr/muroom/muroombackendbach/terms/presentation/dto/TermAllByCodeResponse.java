package kr.muroom.muroombackendbach.terms.presentation.dto;

import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;

public record TermAllByCodeResponse(
    Long termId,
    TermsType code,
    TargetRole targetRole,
    String version,
    boolean isMandatory,
    OffsetDateTime effectiveAt
) {

}
