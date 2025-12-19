package kr.muroom.muroombackendbach.user.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "이메일 중복 확인 응답 DTO",
    description = "이메일 중복 확인 응답 DTO"
)
public record EmailCheckResponse(
    @Schema(example = "true")
    boolean available,

    @Schema(example = "사용 가능한 이메일입니다.")
    String message
) {

}