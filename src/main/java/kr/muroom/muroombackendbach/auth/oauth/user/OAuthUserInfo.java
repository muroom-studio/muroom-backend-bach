package kr.muroom.muroombackendbach.auth.oauth.user;

import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;

public interface OAuthUserInfo {
    String getProviderId();
}
