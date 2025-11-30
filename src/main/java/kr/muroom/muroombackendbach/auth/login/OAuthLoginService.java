package kr.muroom.muroombackendbach.auth.login;

import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
import kr.muroom.muroombackendbach.auth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import kr.muroom.muroombackendbach.user.domain.entity.SocialAccount;
import kr.muroom.muroombackendbach.user.domain.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

  private final SocialAccountRepository socialAccountRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional(readOnly = true)
  public OAuthLoginResponse login(OAuthLoginRequest request) {
    OAuthProvider provider = OAuthProvider.fromRegistrationId(request.provider());

    SocialAccount socialAccount = socialAccountRepository
        .findByProviderAndProviderUserId(provider, request.providerId())
        .orElse(null);

    // 기존 정보가 없을 경우 → 회원가입 필요
    if (socialAccount == null) {
      String signupToken = jwtTokenProvider.createSignupToken(
          provider.name(),
          request.providerId()
      );

      return OAuthLoginResponse.signupRequired(signupToken, provider);
    }

    Long userId = socialAccount.getMusician().getId();
    String accessToken = jwtTokenProvider.createToken(userId);
    return OAuthLoginResponse.login(accessToken, userId, provider);
  }
}
