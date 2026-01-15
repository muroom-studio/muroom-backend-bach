package kr.muroom.muroombackendbach.auth.auth.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "musician auth - 뮤지션 인증 API")
public interface MusicianAuthControllerDocs {

  @Operation(
      summary = "뮤지션 로그인 (Swagger 테스트용 - Kakao)",
      description = """
          Swagger 테스트 전용 로그인 API입니다.
          운영/개발 환경에서는 redirect 기반 로그인 플로우를 사용해야 합니다.
          
          아래 카카오 로그인 페이지에서 인증 후,
          리다이렉트 URL에 포함된 `code` 값을 `providerId` 필드에 입력해주세요.
          
          🔗 카카오 로그인 페이지
          [카카오 로그인 바로가기](https://kauth.kakao.com/oauth/authorize?client_id=a87a624a98805882ce612eed7c018237&redirect_uri=http://localhost:3001/redirect/oauth/kakao&response_type=code)
          """
  )
  ApiResponse<OAuthLoginResponse> oauthLoginForSwaggerByKakao(
      @Valid @RequestBody OAuthLoginRequest request,
      @Parameter(
          description = "OAuth redirect_uri의 origin 값",
          example = "http://localhost:3001"
      )
      @RequestParam String origin,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  );

  @Operation(
      summary = "뮤지션 로그인 (Swagger 테스트용 - Google)",
      description = """
          Swagger 테스트 전용 로그인 API입니다.
          운영/개발 환경에서는 redirect 기반 로그인 플로우를 사용해야 합니다.
          
          아래 구글 로그인 페이지에서 인증 후,
          리다이렉트 URL에 포함된 `code` 값을 `providerId` 필드에 입력해주세요.
          
          🔗 구글 로그인 페이지
          <https://accounts.google.com/o/oauth2/v2/auth?scope=openid%20email%20profile&response_type=code&redirect_uri=http://localhost:3001/redirect/oauth/google&client_id=857075964668-3uqbevha9k2ctfrr6rd272jj9h637ce8.apps.googleusercontent.com>
          """
  )
  ApiResponse<OAuthLoginResponse> oauthLoginForSwaggerByGoogle(
      @Valid @RequestBody OAuthLoginRequest request,
      @Parameter(
          description = "OAuth redirect_uri의 origin 값",
          example = "http://localhost:3001"
      )
      @RequestParam String origin,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  );

  @Operation(
      summary = "로그아웃",
      description = """
          현재 로그인한 뮤지션의 소셜 토큰을 만료(삭제)합니다.
          클라이언트는 JWT를 로컬에서 삭제해야 합니다.
          
          - 요청 바디(refreshToken)가 존재하면 해당 Refresh Token을 서버에서 폐기합니다.
          - Refresh Token의 소유자가 현재 로그인 사용자와 다르면 요청이 거부됩니다.
          """
  )
  @SecurityRequirement(name = "Authentication")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "로그아웃 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "리프레시 토큰 소유자 불일치",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  name = "Refresh Token 소유자 불일치",
                  value = """
                      {
                        "code": "JWT-403-01",
                        "message": "리프레시 토큰의 소유자가 아닙니다."
                      }
                      """,
                  description = """
                      - 전달된 Refresh Token의 소유자(musicianId)와
                        현재 인증된 사용자(@CurrentUserId)의 musicianId가 다른 경우
                      """
              )
          )
      )
  })
  ApiResponse<Void> logout(
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse);
}
