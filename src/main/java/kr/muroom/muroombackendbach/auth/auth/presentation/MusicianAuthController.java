package kr.muroom.muroombackendbach.auth.auth.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.auth.domain.entity.UserType;
import kr.muroom.muroombackendbach.auth.auth.presentation.docs.MusicianAuthControllerDocs;
import kr.muroom.muroombackendbach.auth.auth.presentation.dto.request.LogoutRequest;
import kr.muroom.muroombackendbach.auth.config.CurrentUserId;
import kr.muroom.muroombackendbach.auth.jwt.RefreshTokenService;
import kr.muroom.muroombackendbach.auth.jwt.RefreshTokenService.TokenPair;
import kr.muroom.muroombackendbach.auth.oauth.login.application.OAuthLoginService;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginResponse.ResultType;
import kr.muroom.muroombackendbach.auth.session.SessionAuthService;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/musician")
@RequiredArgsConstructor
@Slf4j
public class MusicianAuthController implements MusicianAuthControllerDocs {

  private final OAuthLoginService oAuthLoginService;
  private final RefreshTokenService refreshTokenService;
  private final SessionAuthService sessionAuthService;

  @PostMapping("/login")
  public ApiResponse<OAuthLoginResponse> oauthLogin(
      @Valid @RequestBody OAuthLoginRequest request,
      @RequestHeader(value = "Origin") String origin, HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  ) {
    OAuthLoginResponse response = oAuthLoginService.login(request, origin);

    if (response.type() == ResultType.LOGIN) {
      sessionAuthService.login(httpRequest, httpResponse, UserType.MUSICIAN,
          Long.valueOf(response.userId()));
    }

    return ApiResponse.success(response);
  }

  @PostMapping("/login/kakao/swagger")
  public ApiResponse<OAuthLoginResponse> oauthLoginForSwaggerByKakao(
      @Valid @RequestBody OAuthLoginRequest request, @RequestParam String origin,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  ) {
    OAuthLoginResponse response = oAuthLoginService.login(request, origin);

    if (response.type() == ResultType.LOGIN) {
      sessionAuthService.login(httpRequest, httpResponse, UserType.MUSICIAN,
          Long.valueOf(response.userId()));
    }

    return ApiResponse.success(response);
  }

  @PostMapping("/login/google/swagger")
  public ApiResponse<OAuthLoginResponse> oauthLoginForSwaggerByGoogle(
      @Valid @RequestBody OAuthLoginRequest request,
      @RequestParam String origin, HttpServletRequest httpRequest, HttpServletResponse httpResponse
  ) {
    OAuthLoginResponse response = oAuthLoginService.login(request, origin);

    if (response.type() == ResultType.LOGIN) {
      sessionAuthService.login(httpRequest, httpResponse, UserType.MUSICIAN,
          Long.valueOf(response.userId()));
    }

    return ApiResponse.success(response);
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @PostMapping("/logout")
  public ApiResponse<Void> logout(
      @CurrentUserId Long musicianId,
      @RequestBody(required = false) LogoutRequest request
  ) {
    String refreshToken = request != null ? request.refreshToken() : null;
    oAuthLoginService.logout(musicianId, refreshToken);
    return ApiResponse.success();
  }

  @PostMapping("/refresh")
  public ApiResponse<TokenPair> refresh(@RequestHeader("refreshToken") String refreshToken) {
    return ApiResponse.success(refreshTokenService.rotate(refreshToken));
  }
}
