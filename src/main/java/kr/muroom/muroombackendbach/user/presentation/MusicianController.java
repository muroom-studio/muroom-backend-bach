package kr.muroom.muroombackendbach.user.presentation;

import static kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto.MusicianSignUpDto;
import static kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto.MusicianSignUpResponse;
import static kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto.MusicianSimpleProfileResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.oauth.login.application.OAuthLoginService;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.user.application.MusicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/musician")
@RequiredArgsConstructor
@Tag(name = "뮤지션 API", description = "뮤지션 관련 API")
public class MusicianController {

  private final MusicianService musicianService;
  private final OAuthLoginService oAuthLoginService;

  @Operation(summary = "뮤지션 회원가입", description = "뮤지션 회원 정보를 등록합니다.")
  @PostMapping("/register")
  public ApiResponse<MusicianSignUpResponse> registerMusician(
      @Valid @RequestBody MusicianSignUpDto request
  ) {
    MusicianSignUpResponse response = musicianService.registerMusician(request);
    return ApiResponse.created(response);
  }

  @Operation(summary = "뮤지션 로그인", description = "인가코드를 기반으로 로그인 시도")
  @PostMapping("/login")
  public ApiResponse<OAuthLoginResponse> oauthLogin(
      @Valid @RequestBody OAuthLoginRequest request,
      @RequestHeader(value = "Origin", required = true) String origin
  ) {
    return ApiResponse.success(oAuthLoginService.login(request, origin));
  }

  @Operation(
      summary = "로그아웃",
      description = "현재 로그인한 뮤지션의 소셜 토큰을 만료(삭제)합니다. 클라이언트는 JWT를 로컬에서 삭제해야 합니다."
  )
  @SecurityRequirement(name = "Bearer Authentication")
  @PostMapping("/logout")
  public ApiResponse<Void> logout(
      // @AuthenticationPrincipal
      Long musicianId
  ) {
    oAuthLoginService.logout(musicianId);
    return ApiResponse.success();
  }

  @Operation(
      summary = "내 간략 정보(프로필 이미지, 닉네임) 조회",
      description = "내 간략 정보(프로필 이미지, 닉네임)를 조회합니다."
  )
  @SecurityRequirement(name = "Bearer Authentication")
  @GetMapping("/me")
  public ApiResponse<MusicianSimpleProfileResponse> getMySimpleProfile(
      @AuthenticationPrincipal Long musicianId
  ) {
    MusicianSimpleProfileResponse response = musicianService.getMusicianSimpleProfile(musicianId);
    return ApiResponse.success(response);
  }
}
