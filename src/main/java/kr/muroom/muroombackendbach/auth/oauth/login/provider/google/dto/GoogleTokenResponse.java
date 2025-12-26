package kr.muroom.muroombackendbach.auth.oauth.login.provider.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record GoogleTokenResponse(
    @JsonProperty("sub")
    String sub,

    @JsonProperty("name")
    String name,

    @JsonProperty("email")
    String email,

    @JsonProperty("email_verified")
    Boolean emailVerified,

    @JsonProperty("picture")
    String picture
) {

}
