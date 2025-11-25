package kr.muroom.muroombackendbach.auth.oauth.util;

import kr.muroom.muroombackendbach.auth.oauth.userinfo.GoogleUserInfo;
import kr.muroom.muroombackendbach.auth.oauth.userinfo.KakaoUserInfo;
import kr.muroom.muroombackendbach.auth.oauth.userinfo.NaverUserInfo;
import kr.muroom.muroombackendbach.auth.oauth.userinfo.OAuthUserInfo;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;

import java.util.Map;

public class OAuthUserInfoFactory {

    public static OAuthUserInfo of(OAuthProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case KAKAO -> new KakaoUserInfo(attributes);
            case NAVER -> new NaverUserInfo(attributes);
            case GOOGLE -> new GoogleUserInfo(attributes);
        };
    }
}