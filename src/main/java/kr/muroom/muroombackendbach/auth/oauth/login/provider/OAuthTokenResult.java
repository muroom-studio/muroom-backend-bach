package kr.muroom.muroombackendbach.auth.oauth.login.provider;

import lombok.Builder;

@Builder
public record OAuthTokenResult(
    String idToken
) {

}
