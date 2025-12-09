package kr.muroom.muroombackendbach.auth.oauth.token;

import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("social_token")
public class SocialToken {

  @Id
  private String id;
  private Long userId;
  private OAuthProvider provider;
  private String accessToken;
  private String refreshToken;
  private Long accessTokenExpireTime;
  private Long refreshTokenExpireTime;
}
