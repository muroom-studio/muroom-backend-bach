package kr.muroom.muroombackendbach.auth.oauth.login.provider.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoTokenResponse(

    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("id_token")
    String idToken,

    @JsonProperty("expires_in")
    Long expiresIn,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("refresh_token_expires_in")
    Long refreshTokenExpiresIn,

    @JsonProperty("scope")
    String scope,

    @JsonProperty("error")
    String error,

    @JsonProperty("error_description")
    String errorDescription
) {

}
