package kr.muroom.muroombackendbach.terms.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;

public record TermRegisterRequest(
    @NotNull
    TermsType code,

    @NotNull
    TargetRole targetRole,

    Boolean isMandatory,

    @NotNull
    OffsetDateTime effectiveAt,

    @NotBlank(message = "약관 내용을 채워주세요")
    String content
) {

}
