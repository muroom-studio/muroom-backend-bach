package kr.muroom.muroombackendbach.user.presentation;

import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.oauth.login.application.OAuthLoginService;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.user.application.MusicianService;
import kr.muroom.muroombackendbach.user.presentation.docs.MusicianControllerDocs;
import kr.muroom.muroombackendbach.user.presentation.dto.request.LogoutRequest;
import kr.muroom.muroombackendbach.user.presentation.dto.request.MusicianSignupRequest;
import kr.muroom.muroombackendbach.user.presentation.dto.request.UpdateMusicianProfileRequest;
import kr.muroom.muroombackendbach.user.presentation.dto.response.MusicianProfileResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.response.MusicianSignupResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.response.MusicianSimpleProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/musician")
@RequiredArgsConstructor
public class MusicianController implements MusicianControllerDocs {

  private final MusicianService musicianService;
  private final OAuthLoginService oAuthLoginService;

  @PostMapping("/register")
  public ApiResponse<MusicianSignupResponse> registerMusician(
      @Valid @RequestBody MusicianSignupRequest request
  ) {
    MusicianSignupResponse response = musicianService.registerMusician(request);
    return ApiResponse.created(response);
  }

  @PostMapping("/login")
  public ApiResponse<OAuthLoginResponse> oauthLogin(
      @Valid @RequestBody OAuthLoginRequest request,
      @RequestHeader(value = "Origin") String origin
  ) {
    return ApiResponse.success(oAuthLoginService.login(request, origin));
  }

  @PostMapping("/login/kakao/swagger")
  public ApiResponse<OAuthLoginResponse> oauthLoginForSwaggerByKakao(
      @Valid @RequestBody OAuthLoginRequest request,
      @RequestParam String origin
  ) {
    return ApiResponse.success(oAuthLoginService.login(request, origin));
  }

  @PostMapping("/login/google/swagger")
  public ApiResponse<OAuthLoginResponse> oauthLoginForSwaggerByGoogle(
      @Valid @RequestBody OAuthLoginRequest request,
      @RequestParam String origin
  ) {
    return ApiResponse.success(oAuthLoginService.login(request, origin));
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/logout")
  public ApiResponse<Void> logout(
      @AuthenticationPrincipal Long musicianId,
      @RequestBody(required = false) LogoutRequest request
  ) {
    String refreshToken = request != null ? request.refreshToken() : null;
    oAuthLoginService.logout(musicianId, refreshToken);
    return ApiResponse.success();
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public ApiResponse<MusicianSimpleProfileResponse> getMySimpleProfile(
      @AuthenticationPrincipal Long musicianId
  ) {
    MusicianSimpleProfileResponse response = musicianService.getMusicianSimpleProfile(musicianId);
    return ApiResponse.success(response);
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me/detail")
  public ApiResponse<MusicianProfileResponse> getMyProfile(
      @AuthenticationPrincipal Long musicianId
  ) {
    return ApiResponse.success(musicianService.getMusicianProfile(musicianId));
  }

  @PreAuthorize("isAuthenticated()")
  @PatchMapping("/me/detail")
  public ApiResponse<Void> updateMyProfile(
      @AuthenticationPrincipal Long musicianId,
      @Valid @RequestBody UpdateMusicianProfileRequest request
  ) {
    musicianService.updateMyProfile(musicianId, request);
    return ApiResponse.success();
  }
}
