package kr.muroom.muroombackendbach.auth.oauth.userinfo;

import java.util.Map;

public record NaverUserInfo(
        Map<String, Object> attributes
) implements OAuthUserInfo {

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        return id != null ? id.toString() : null;
    }
}
