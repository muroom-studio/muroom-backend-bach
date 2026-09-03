package kr.muroom.muroombackendbach.auth.oauth.login.provider.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoIdTokenPayload {

  private String aud;
  private String sub;
  @JsonProperty("auth_time")
  private Long authTime;
  private String iss;
  private Long exp;
  private Long iat;
  private String nickname;
  private String picture;
  private String email;
}