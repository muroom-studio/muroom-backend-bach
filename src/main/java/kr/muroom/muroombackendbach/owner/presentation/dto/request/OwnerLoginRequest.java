package kr.muroom.muroombackendbach.owner.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
    name = "사장님 로그인 요청 DTO",
    description = "사장님 로그인 요청 DTO"
)
public record OwnerLoginRequest(
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(example = "owner@example.com")
    String email,

    @NotBlank
    @Schema(example = "password1234")
    String password
) {

}
