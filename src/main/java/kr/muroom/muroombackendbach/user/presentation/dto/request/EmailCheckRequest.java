package kr.muroom.muroombackendbach.user.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
    name = "이메일 중복 확인 요청 DTO",
    description = "이메일 중복 확인 요청 DTO"
)
public record EmailCheckRequest(
    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(example = "owner@example.com")
    String email
) {

}
