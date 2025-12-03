package kr.muroom.muroombackendbach.auth.oauth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoIdTokenPayload {

  private String aud;
  private String sub;
  private Long auth_time;
  private String iss;
  private Long exp;
  private Long iat;
  private String nickname;
  private String picture;
  private String email;
}