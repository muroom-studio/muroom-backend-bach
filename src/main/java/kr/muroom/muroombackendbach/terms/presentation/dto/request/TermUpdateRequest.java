package kr.muroom.muroombackendbach.terms.presentation.dto.request;

import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;

public record TermUpdateRequest(
    TermsType code,
    TargetRole targetRole,
    boolean isMandatory,
    OffsetDateTime effectiveAt,
    String content
) {

}
