package kr.muroom.muroombackendbach.auth.oauth.login.provider.google.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleIdTokenPayload {

  private String iss;
  private String aud;
  private String sub;

  private String email;
  private Boolean emailVerified;

  private String name;
  private String picture;

  private Long exp;
  private Long iat;
}