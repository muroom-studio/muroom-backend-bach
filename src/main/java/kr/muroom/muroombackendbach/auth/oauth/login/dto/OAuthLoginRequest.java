package kr.muroom.muroombackendbach.auth.oauth.login.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
    @NotBlank
    String provider,
    @NotBlank
    String providerId
) {

}
