package kr.muroom.muroombackendbach.auth.oauth.login.provider.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class GoogleTokenResponse {

  @JsonProperty("sub")
  private String sub;

  @JsonProperty("name")
  private String name;

  @JsonProperty("email")
  private String email;

  @JsonProperty("email_verified")
  private Boolean emailVerified;

  @JsonProperty("picture")
  private String picture;
}
