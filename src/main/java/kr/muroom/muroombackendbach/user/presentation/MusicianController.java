package kr.muroom.muroombackendbach.user.presentation;

import static kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto.MusicianSignUpDto;
import static kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto.MusicianSignUpResponse;
import static kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto.MusicianSimpleProfileResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.oauth.login.application.OAuthLoginService;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.user.application.MusicianService;
import kr.muroom.muroombackendbach.user.presentation.docs.MusicianControllerDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
  public ApiResponse<MusicianSignUpResponse> registerMusician(
      @Valid @RequestBody MusicianSignUpDto request
  ) {
    MusicianSignUpResponse response = musicianService.registerMusician(request);
    return ApiResponse.created(response);
  }

  @PostMapping("/login")
  public ApiResponse<OAuthLoginResponse> oauthLogin(
      @Valid @RequestBody OAuthLoginRequest request,
      @RequestHeader(value = "Origin") String origin
  ) {
    return ApiResponse.success(oAuthLoginService.login(request, origin));
  }

  @PostMapping("/login/swagger")
  public ApiResponse<OAuthLoginResponse> oauthLoginForSwagger(
      @Valid @RequestBody OAuthLoginRequest request,
      @RequestParam String origin
  ) {
    return ApiResponse.success(oAuthLoginService.login(request, origin));
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(
      @AuthenticationPrincipal
      Long musicianId
  ) {
    oAuthLoginService.logout(musicianId);
    return ApiResponse.success();
  }

  @GetMapping("/me")
  public ApiResponse<MusicianSimpleProfileResponse> getMySimpleProfile(
      @AuthenticationPrincipal Long musicianId
  ) {
    MusicianSimpleProfileResponse response = musicianService.getMusicianSimpleProfile(musicianId);
    return ApiResponse.success(response);
  }
}
