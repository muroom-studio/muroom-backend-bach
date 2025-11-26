package kr.muroom.muroombackendbach.auth.oauth.userinfo;


import java.util.Map;

public record GoogleUserInfo(
        Map<String, Object> attributes
) implements OAuthUserInfo {

    @Override
    public String getProviderId() {
        Object id = attributes.get("sub");
        return id != null ? id.toString() : null;
    }
}
