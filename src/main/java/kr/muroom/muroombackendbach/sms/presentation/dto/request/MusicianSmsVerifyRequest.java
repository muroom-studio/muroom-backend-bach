package kr.muroom.muroombackendbach.sms.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "문자 인증 검증", description = "SMS 인증번호 검증 요청 DTO")
public record MusicianSmsVerifyRequest(
    @Schema(description = "인증코드 받았던 전화번호", example = "010-1234-5678")
    @NotBlank
    String phone,

    @Schema(description = "인증번호 6자리", example = "123456")
    @Size(min = 6, max = 6)
    @NotBlank
    String code
) {

}
