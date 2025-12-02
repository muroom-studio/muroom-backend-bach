package kr.muroom.muroombackendbach.user.presentation;

import static kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto.MusicianSignUpDto;
import static kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto.MusicianSignUpResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.login.OAuthLoginService;
import kr.muroom.muroombackendbach.auth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.user.application.MusicianService;
import kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
      @Valid @RequestBody MusicianSignUpDto musicianSignUpRequest) {
    MusicianSignUpResponse response =
        musicianService.registerMusician(musicianSignUpRequest);
    return ApiResponse.created(response);
  }

  @Operation(summary = "뮤지션 로그인", description = "인가코드를 기반으로 로그인 시도")
  @PostMapping("/login")
  public ApiResponse<OAuthLoginResponse> oauthLogin(@RequestBody OAuthLoginRequest request) {
    return ApiResponse.success(oAuthLoginService.login(request));
  }

  //내 간략 정보(프로필 이미지, 닉네임) 조회
  @GetMapping("/me")
  public ApiResponse<MusicianDto.MusicianSimpleProfileResponse> getNickname(@AuthenticationPrincipal Long musicianId) {
    MusicianDto.MusicianSimpleProfileResponse response = musicianService.getMusicianSimpleProfile(musicianId);
    return ApiResponse.success(response);
  }
}
