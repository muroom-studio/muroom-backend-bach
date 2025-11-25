package kr.muroom.muroombackendbach.auth.oauth.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public record UnregisteredOAuth2User(
        String provider,
        String providerId,
        Map<String, Object> attributes
) implements OAuth2User {

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_OAUTH_PENDING"));
    }

    @Override
    public String getName() {
        return provider + ":" + providerId;
    }
}
