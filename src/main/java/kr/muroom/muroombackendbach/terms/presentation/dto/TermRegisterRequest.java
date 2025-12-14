package kr.muroom.muroombackendbach.terms.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;

public record TermRegisterRequest(
    @Schema(example = "TERMS_OF_USE", description =
        "TERMS_OF_USE(이용 약관), PRIVACY_COLLECTION(개인정보 수집), MARKETING_RECEIVE(마켓팅 "
            + "동의) 중 택일")
    @NotNull
    TermsType code,

    @Schema(example = "OWNER", description = "OWNER(사장님), MUSICIAN(일반 유저) 중 택일 ")
    @NotNull
    TargetRole targetRole,

    @Schema(example = "true", description = "약관 동의 필수 유무")
    @NotNull
    Boolean isMandatory,

    @Schema(example = "2025-12-06 15:00:00.000000 +00:00", description = "약관 시행일")
    @NotNull
    OffsetDateTime effectiveAt,

    @Schema(example = "사용자 이용 동의 약관", description = "약관 제목")
    @NotBlank(message = "약관 제목을 채워주세요")
    String title,

    @Schema(example = "1조항 ~~~", description = "약관 내용 Markdown 내용")
    @NotBlank(message = "약관 내용을 채워주세요")
    String content
) {

}
