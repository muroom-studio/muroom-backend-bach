package kr.muroom.muroombackendbach.user.domain.entity;

import static kr.muroom.muroombackendbach.auth.oauth.login.exception.OAuthLoginErrorCode.UNSUPPORTED_OAUTH_PROVIDER;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.muroom.muroombackendbach.common.domain.EnumMapperType;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OAuthProvider implements EnumMapperType {
  KAKAO("카카오"), NAVER("네이버"), GOOGLE("구글");

  private final String description;

  @Override
  public String getCode() {
    return name();
  }

  public static OAuthProvider fromRegistrationId(String registrationId) {
    return switch (registrationId.toUpperCase()) {
      case "KAKAO" -> KAKAO;
      case "GOOGLE" -> GOOGLE;
      case "NAVER" -> NAVER;
      default -> throw new BusinessException(UNSUPPORTED_OAUTH_PROVIDER);
    };
  }
}
