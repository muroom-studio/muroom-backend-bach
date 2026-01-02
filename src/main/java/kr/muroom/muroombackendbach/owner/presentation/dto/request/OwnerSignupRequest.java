package kr.muroom.muroombackendbach.owner.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "사장님 회원가입 요청 DTO")
public record OwnerSignupRequest(
    @NotBlank(message = "이름을 입력해주세요.")
    @Schema(example = "홍길동")
    String name,

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(example = "owner@example.com")
    String email,

    @NotBlank
    @Schema(example = "password1234")
    String password,

    @Schema(example = "1990-05-12")
    LocalDate birthdate,

    @Schema(example = "010-1234-5678", nullable = true)
    String phoneNumber,

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Schema(example = "뮤지션사장님")
    String nickname,

    @Schema(example = "[1, 2, 3]", nullable = true)
    List<Long> termIds

) {

}
