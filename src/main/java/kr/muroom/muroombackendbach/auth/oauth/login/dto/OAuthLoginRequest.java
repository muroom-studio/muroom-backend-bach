package kr.muroom.muroombackendbach.auth.oauth.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
    @Schema(example = "kakao", defaultValue = "kakao")
    @NotBlank
    String provider,
    @NotBlank
    String providerId
) {

}
