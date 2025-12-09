package kr.muroom.muroombackendbach.auth.oauth.login.provider;

import lombok.Builder;

@Builder
public record OAuthTokenResult(
    String accessToken,
    String refreshToken,
    Long accessTokenExpiresIn,
    Long refreshTokenExpiresIn,
    String idToken
) {

}
