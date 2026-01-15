package kr.muroom.muroombackendbach.auth.oauth.login.application;

import static kr.muroom.muroombackendbach.auth.jwt.exception.JwtErrorCode.MISMATCH_REFRESH_TOKEN_OWNER;
import static kr.muroom.muroombackendbach.musician.exception.MusicianErrorCode.MUSICIAN_NOT_FOUND;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.auth.auth.domain.entity.OAuthProvider;
import kr.muroom.muroombackendbach.auth.auth.domain.entity.SocialAccount;
import kr.muroom.muroombackendbach.auth.auth.domain.repository.SocialAccountRepository;
import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.OAuthClientService;
import kr.muroom.muroombackendbach.auth.oauth.login.provider.OAuthTokenResult;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthLoginService {

  private final SocialAccountRepository socialAccountRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final List<OAuthClientService> clients;

  private Map<OAuthProvider, OAuthClientService> clientMap;

  @PostConstruct
  void initClientMap() {
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
        .map(this::loginExistingUser)
        .orElseGet(() -> prepareSignup(provider, providerUserId));
  }

  /**
   * 기존 계정 로그인 처리: - Musician ID 조회 - 소셜 토큰 Redis 저장 - 우리 서비스 JWT 발급
   */
  private OAuthLoginResponse loginExistingUser(
      SocialAccount socialAccount
  ) {
    Long userId = Optional.ofNullable(socialAccount.getMusician())
        .map(Musician::getId)
        .orElseThrow(() -> new BusinessException(MUSICIAN_NOT_FOUND));

    return OAuthLoginResponse.login(String.valueOf(userId),
        socialAccount.getProvider());
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
