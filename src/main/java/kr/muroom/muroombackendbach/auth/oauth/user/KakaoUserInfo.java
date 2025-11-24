package kr.muroom.muroombackendbach.auth.oauth.user;

import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class KakaoUserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        return id != null ? id.toString() : null;
    }
}
