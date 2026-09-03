package kr.muroom.muroombackendbach.owner.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OwnerSignupRequest(
    @NotBlank(message = "이름을 입력해주세요.")
    String name,

    @Email
    @NotBlank
    String email,

    @NotBlank
    String password,

    @NotBlank(message = "전화번호 토큰값을 입력해주세요.")
    String smsVerifyToken,

    @NotBlank(message = "닉네임을 입력해주세요.")
    String nickname,

    @NotEmpty(message = "약관 동의 항목을 선택해주세요.")
    List<Long> termIds
) {

}
