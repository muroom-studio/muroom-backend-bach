package kr.muroom.muroombackendbach.terms.presentation.dto.response;

import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;
import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;

public record TermAllByCodeResponse(
    String termId,
    TermsType code,
    TargetRole targetRole,
    String version,
    boolean isMandatory,
    OffsetDateTime effectiveAt
) {

  public static TermAllByCodeResponse from(Term term) {
    return new TermAllByCodeResponse(
        String.valueOf(term.getId()),
        term.getCode(),
        term.getTargetRole(),
        term.getVersion(),
        term.isMandatory(),
        term.getEffectiveAt()
    );
  }
}
