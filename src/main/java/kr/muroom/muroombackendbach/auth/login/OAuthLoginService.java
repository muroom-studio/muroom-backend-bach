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

    if (provider != OAuthProvider.KAKAO) {
      throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + provider);
    }

    // 1. 인가 코드로 카카오 회원 ID(sub) 조회
    String providerUserId = getKakaoUserIdFromAuthorizationCode(request.providerId(), baseUrl);

    // 2. 기존 계정 로그인 or 회원가입 필요 응답
    return loginOrPrepareSignup(provider, providerUserId);
  }

  /**
   * 인가 코드와 baseUrl을 사용하여 카카오 ID 토큰을 얻고, ID 토큰의 sub(카카오 회원 번호)를 가져온다.
   */
  private String getKakaoUserIdFromAuthorizationCode(String authorizationCode, String baseUrl) {
    String redirectUri = baseUrl + "/redirect/oauth/kakao";

    KakaoTokenResponse tokenResponse =
        kakaoOAuthClient.exchangeCodeForToken(authorizationCode, redirectUri);

    String idToken = tokenResponse.getIdToken();
    if (idToken == null) {
      throw new IllegalStateException("카카오 ID Token 이 존재하지 않습니다.");
    }

    KakaoIdTokenPayload payload = kakaoIdTokenDecoder.decode(idToken);
    return payload.getSub(); // 카카오 회원 고유 ID
  }

  /**
   * provider + providerUserId로 기존 계정을 찾고, 있으면 로그인, 없으면 회원가입 필요 응답을 만든다.
   */
  private OAuthLoginResponse loginOrPrepareSignup(OAuthProvider provider, String providerUserId) {
    return socialAccountRepository
        .findByProviderAndProviderUserId(provider, providerUserId)

        // 로그인
        .map(socialAccount -> {
          Long userId = ofNullable(socialAccount.getMusician())
              .map(Musician::getId)
              .orElseThrow(() -> new BusinessException(MUSICIAN_NOT_FOUND));

          String accessToken = jwtTokenProvider.createToken(userId);
          return OAuthLoginResponse.login(accessToken, userId, provider);
        })

        // 회원 가입 필요
        .orElseGet(() -> {
          String signupToken = jwtTokenProvider.createSignupToken(
              provider.name(),
              providerUserId
          );
          return OAuthLoginResponse.signupRequired(signupToken, provider);
        });
  }
}
