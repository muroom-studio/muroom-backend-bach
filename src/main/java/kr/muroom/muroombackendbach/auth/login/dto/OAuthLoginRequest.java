package kr.muroom.muroombackendbach.auth.login.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
    @NotBlank
    String provider,
    @NotBlank
    String providerId
) {

}
