package kr.muroom.muroombackendbach.sms.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "문자 인증 전송 요청", description = "SMS 인증번호 전송 요청 DTO")
public record MusicianSmsSendRequest(
    @Schema(description = "전화번호(하이픈 붙여야함)", example = "010-1234-5678")
    @NotBlank
    String phone
) {

}
