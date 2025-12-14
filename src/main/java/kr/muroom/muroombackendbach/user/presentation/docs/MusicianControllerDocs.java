package kr.muroom.muroombackendbach.user.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto;
import kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto.MusicianSignUpResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto.MusicianSimpleProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "뮤지션 API", description = "뮤지션 관련 API")
public interface MusicianControllerDocs {

  @Operation(
      summary = "뮤지션 회원가입",
      description = "뮤지션 회원 정보를 등록합니다."
  )
  ApiResponse<MusicianSignUpResponse> registerMusician(
      @Valid @RequestBody MusicianDto.MusicianSignUpDto request
  );

  @Operation(
      summary = "뮤지션 로그인",
      description = "인가 코드를 기반으로 소셜 로그인을 시도합니다."
  )
  ApiResponse<OAuthLoginResponse> oauthLogin(
      @Valid @RequestBody OAuthLoginRequest request,
      @RequestHeader(value = "Origin") String origin
  );

  @Operation(
      summary = "뮤지션 로그인 (Swagger 테스트용)",
      description = "Swagger 테스트 전용 로그인 API입니다. "
          + "운영/개발 환경에서는 redirect 기반 로그인 플로우를 사용해야 합니다."
  )
  ApiResponse<OAuthLoginResponse> oauthLoginForSwagger(
      @Valid @RequestBody OAuthLoginRequest request,
      @RequestParam String origin
  );

  @Operation(
      summary = "로그아웃",
      description =
          """
              현재 로그인한 뮤지션의 소셜 토큰을 만료(삭제)합니다.
              클라이언트는 JWT를 로컬에서 삭제해야 합니다.
              
              - 요청 쿠키(refresh)가 존재하면 해당 Refresh Token을 서버에서 폐기합니다.
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
              examples = {
                  @ExampleObject(
                      name = "Refresh Token 소유자 불일치",
                      value = """
                          {
                            "code": "JWT-403-01",
                            "message": "리프레시 토큰의 소유자가 아닙니다."
                          }
                          """,
                      description =
                          """
                              - 쿠키(refresh)로 전달된 Refresh Token의 musicianId(소유자)와
                                현재 인증된 사용자(@AuthenticationPrincipal)의 musicianId가 다른 경우
                              """
                  )
              }
          )
      )
  })
  @SecurityRequirement(name = "Authentication")
  ApiResponse<Void> logout(
      @AuthenticationPrincipal Long musicianId,
      @CookieValue(name = "refresh", required = false) String refreshToken
  );

  @Operation(
      summary = "내 간략 정보 조회",
      description = "현재 로그인한 뮤지션의 간략 정보(프로필 이미지, 닉네임)를 조회합니다."
  )
  @SecurityRequirement(name = "Authentication")
  ApiResponse<MusicianSimpleProfileResponse> getMySimpleProfile(
      @AuthenticationPrincipal Long musicianId
  );
}