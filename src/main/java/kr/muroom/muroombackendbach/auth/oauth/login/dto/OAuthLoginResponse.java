package kr.muroom.muroombackendbach.auth.oauth.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;

@Schema(name = "소셜 로그인 응답", description = "OAuth 로그인 결과 응답. 로그인 성공 또는 회원가입 필요 여부를 나타냅니다.")
public record OAuthLoginResponse(

    @Schema(description = "로그인 결과 타입 (LOGIN: 기존 회원 로그인, SIGNUP_REQUIRED: 회원가입 필요)", example =
        "LOGIN")
    ResultType type,

    @Schema(
        description = "엑세스 토큰 (로그인 성공 시에만 반환)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        nullable = true
    )
    String accessToken,

    @Schema(
        description = "리프레쉬 토큰 (로그인 성공 시에만 반환)",
        nullable = true
    )
    String refreshToken,

    @Schema(
        description = "회원가입 진행을 위한 임시 토큰 (회원가입 필요 시에만 반환)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        nullable = true
    )
    String signupToken,

    @Schema(
        description = "로그인된 유저 ID (로그인 성공 시에만 반환)",
        example = "1",
        nullable = true
    )
    String userId,

    @Schema(
        description = "OAuth 제공자",
        example = "KAKAO"
    )
    OAuthProvider provider
) {

  @Schema(
      description = "OAuth 로그인 결과 타입",
      example = "LOGIN"
  )
  public enum ResultType {
    LOGIN,
    SIGNUP_REQUIRED
  }

  // 로그인 성공용 팩토리 메서드
  public static OAuthLoginResponse login(String accessToken, String refreshToken, String userId,
      OAuthProvider provider) {
    return new OAuthLoginResponse(ResultType.LOGIN, accessToken, refreshToken, null, userId,
        provider);
  }

  // 회원가입 필요용 팩토리 메서드
  public static OAuthLoginResponse signupRequired(String signupToken, OAuthProvider provider) {
    return new OAuthLoginResponse(ResultType.SIGNUP_REQUIRED, null, null, signupToken, null,
        provider);
  }
}
