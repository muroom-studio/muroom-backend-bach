package kr.muroom.muroombackendbach.auth.login;

import static java.util.Optional.*;
import static kr.muroom.muroombackendbach.user.exception.MusicianErrorCode.NOT_EXIST_MUSICIAN;

import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
import kr.muroom.muroombackendbach.auth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
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

    return socialAccountRepository
        .findByProviderAndProviderUserId(provider, request.providerId())

        // 로그인
        .map(socialAccount -> {
          Long userId = ofNullable(socialAccount.getMusician())
              .map(Musician::getId)
              .orElseThrow(() -> new BusinessException(NOT_EXIST_MUSICIAN));
          String accessToken = jwtTokenProvider.createToken(userId);
          return OAuthLoginResponse.login(accessToken, userId, provider);
        })

        // 회원 가입
        .orElseGet(() -> {
          String signupToken = jwtTokenProvider.createSignupToken(
              provider.name(),
              request.providerId()
          );
          return OAuthLoginResponse.signupRequired(signupToken, provider);
        });
  }
}
