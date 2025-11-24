package kr.muroom.muroombackendbach.auth.oauth.service;

import kr.muroom.muroombackendbach.auth.oauth.user.CustomOAuth2User;
import kr.muroom.muroombackendbach.auth.oauth.user.OAuthUserInfo;
import kr.muroom.muroombackendbach.auth.oauth.util.OAuthUserInfoFactory;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import kr.muroom.muroombackendbach.user.domain.entity.SocialAccount;
import kr.muroom.muroombackendbach.user.domain.repository.MusicianRepository;
import kr.muroom.muroombackendbach.user.domain.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialAccountRepository socialAccountRepository;
    private final MusicianRepository musicianRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider provider = OAuthProvider.fromRegistrationId(registrationId);

        OAuthUserInfo oAuthUserInfo = OAuthUserInfoFactory.of(provider, attributes);
        String providerId = oAuthUserInfo.getProviderId();

        SocialAccount socialAccount = socialAccountRepository
                .findByProviderAndProviderUserId(provider, providerId)
                .orElseGet(() -> createSocialAccount(provider, providerId));

        Musician musician = socialAccount.getMusician();

        return new CustomOAuth2User(
                musician.getId(),
                providerId,
                attributes
        );
    }

    private SocialAccount createSocialAccount(OAuthProvider provider, String providerId) {
        Musician musician = Musician.builder()
                .build();
        musicianRepository.save(musician);

        SocialAccount socialAccount = SocialAccount.builder()
                .musician(musician)
                .provider(provider)
                .providerUserId(providerId)
                .build();

        return socialAccountRepository.save(socialAccount);
    }
}
