package kr.muroom.muroombackendbach.auth.oauth.token;

import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialTokenService {

  private final SocialTokenRepository socialTokenRepository;

  public void save(
      Long userId,
      OAuthProvider provider,
      String accessToken,
      String refreshToken,
      Long accessTokenExpireTime,
      Long refreshTokenExpiresIn
  ) {
    SocialToken socialToken = SocialToken.builder()
        .id(buildId(provider, userId))
        .userId(userId)
        .provider(provider)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpireTime(accessTokenExpireTime)
        .refreshTokenExpireTime(refreshTokenExpiresIn)
        .build();

    socialTokenRepository.save(socialToken);
  }

  public void deleteByProviderAndUser(OAuthProvider provider, Long userId) {
    socialTokenRepository.deleteById(buildId(provider, userId));
  }

  public void deleteAllByUser(Long userId) {
    socialTokenRepository.deleteByUserId(userId);
  }

  private String buildId(OAuthProvider provider, Long musicianId) {
    return provider.name() + ":" + musicianId; // ex) KAKAO:1
  }
}
