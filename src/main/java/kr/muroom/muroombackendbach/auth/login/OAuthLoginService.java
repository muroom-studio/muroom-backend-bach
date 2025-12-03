package kr.muroom.muroombackendbach.auth.login;

import static java.util.Optional.ofNullable;
import static kr.muroom.muroombackendbach.user.exception.MusicianErrorCode.MUSICIAN_NOT_FOUND;

import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
import kr.muroom.muroombackendbach.auth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.auth.oauth.KakaoIdTokenDecoder;
import kr.muroom.muroombackendbach.auth.oauth.KakaoIdTokenPayload;
import kr.muroom.muroombackendbach.auth.oauth.KakaoOAuthClient;
import kr.muroom.muroombackendbach.auth.oauth.KakaoTokenResponse;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import kr.muroom.muroombackendbach.user.domain.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

  private final SocialAccountRepository socialAccountRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final KakaoOAuthClient kakaoOAuthClient;
  private final KakaoIdTokenDecoder kakaoIdTokenDecoder;

  @Transactional(readOnly = true)
  public OAuthLoginResponse login(OAuthLoginRequest request, String baseUrl) {
    OAuthProvider provider = OAuthProvider.fromRegistrationId(request.provider());

    KakaoTokenResponse tokenResponse =
        kakaoOAuthClient.exchangeCodeForToken(request.providerId(),
            baseUrl + "/redirect/oauth/kakao");

    String idToken = tokenResponse.getIdToken();
    if (idToken == null) {
      throw new IllegalStateException("카카오 ID Token 이 존재하지 않습니다.");
    }
    KakaoIdTokenPayload payload = kakaoIdTokenDecoder.decode(idToken);

    return socialAccountRepository
        .findByProviderAndProviderUserId(provider, payload.getSub())

        // 로그인
        .map(socialAccount -> {
          Long userId = ofNullable(socialAccount.getMusician())
              .map(Musician::getId)
              .orElseThrow(() -> new BusinessException(MUSICIAN_NOT_FOUND));
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
