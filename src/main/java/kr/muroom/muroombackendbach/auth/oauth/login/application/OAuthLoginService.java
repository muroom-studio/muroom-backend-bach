package kr.muroom.muroombackendbach.auth.oauth.login.application;

import static java.util.Optional.ofNullable;
import static kr.muroom.muroombackendbach.user.exception.MusicianErrorCode.MUSICIAN_NOT_FOUND;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
import kr.muroom.muroombackendbach.auth.oauth.token.SocialTokenService;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.OAuthClientService;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.OAuthTokenResult;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import kr.muroom.muroombackendbach.user.domain.entity.SocialAccount;
import kr.muroom.muroombackendbach.user.domain.repository.SocialAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OAuthLoginService {

  private final SocialTokenService socialTokenService;
  private final SocialAccountRepository socialAccountRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final Map<OAuthProvider, OAuthClientService> clientMap;

  public OAuthLoginService(
      SocialAccountRepository socialAccountRepository,
      JwtTokenProvider jwtTokenProvider,
      SocialTokenService socialTokenService,
      List<OAuthClientService> clients
  ) {
    this.socialAccountRepository = socialAccountRepository;
    this.jwtTokenProvider = jwtTokenProvider;
    this.socialTokenService = socialTokenService;
    this.clientMap = clients.stream()
        .collect(Collectors.toMap(OAuthClientService::getProvider, c -> c));
  }

  @Transactional(readOnly = true)
  public OAuthLoginResponse login(OAuthLoginRequest request, String origin) {

    // 1. provider 파싱 및 OAuthClient 조회
    OAuthProvider provider = OAuthProvider.fromRegistrationId(request.provider());
    OAuthClientService client = clientMap.get(provider);

    // 2. 인가 코드로 외부 토큰 교환 + 소셜 유저 ID 추출
    OAuthTokenResult tokenResult = client.exchangeCode(request.providerId(), origin);
    String providerUserId = client.extractProviderUserId(tokenResult);

    // 3. 기존 소셜 계정 조회
    return socialAccountRepository
        .findByProviderAndProviderUserId(provider, providerUserId)
        .map(account -> loginExistingUser(account, provider, tokenResult))
        .orElseGet(() -> prepareSignup(provider, providerUserId));
  }

  @Transactional
  public void logout(Long musicianId) {
    // TO DO: redis에 토큰 삭제
    return;
  }

  /**
   * 기존 계정 로그인 처리: - Musician ID 조회 - 소셜 토큰 Redis 저장 - 우리 서비스 JWT 발급
   */
  private OAuthLoginResponse loginExistingUser(
      SocialAccount socialAccount,
      OAuthProvider provider,
      OAuthTokenResult tokenResult
  ) {
    Long userId = ofNullable(socialAccount.getMusician())
        .map(Musician::getId)
        .orElseThrow(() -> new BusinessException(MUSICIAN_NOT_FOUND));

    // 소셜 토큰 Redis에 저장
    socialTokenService.save(
        userId,
        provider,
        tokenResult.accessToken(),
        tokenResult.refreshToken(),
        tokenResult.accessTokenExpiresIn(),
        tokenResult.refreshTokenExpiresIn());

    // 우리 서비스 접근 토큰 발급
    String accessToken = jwtTokenProvider.createToken(userId);

    return OAuthLoginResponse.login(accessToken, userId, provider);
  }

  /**
   * 회원가입 필요 응답: - provider + providerUserId로 signupToken 발급
   */
  private OAuthLoginResponse prepareSignup(OAuthProvider provider, String providerUserId) {
    String signupToken = jwtTokenProvider.createSignupToken(
        provider.name(),
        providerUserId
    );

    return OAuthLoginResponse.signupRequired(signupToken, provider);
  }
}
