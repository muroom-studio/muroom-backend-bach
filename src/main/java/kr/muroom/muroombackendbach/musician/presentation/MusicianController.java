package kr.muroom.muroombackendbach.musician.presentation;

import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.annotation.CurrentUserId;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.musician.application.MusicianService;
import kr.muroom.muroombackendbach.musician.presentation.docs.MusicianControllerDocs;
import kr.muroom.muroombackendbach.musician.presentation.dto.request.MusicianSignupRequest;
import kr.muroom.muroombackendbach.musician.presentation.dto.request.UpdateMusicianProfileRequest;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianProfileResponse;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianSignupResponse;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianSimpleProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/musicians")
@RequiredArgsConstructor
public class MusicianController implements MusicianControllerDocs {

  private final MusicianService musicianService;

  @PostMapping("/register")
  public ApiResponse<MusicianSignupResponse> registerMusician(
      @Valid @RequestBody MusicianSignupRequest request
  ) {
    MusicianSignupResponse response = musicianService.registerMusician(request);
    return ApiResponse.created(response);
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @GetMapping("/me")
  public ApiResponse<MusicianSimpleProfileResponse> getMySimpleProfile(
      @CurrentUserId Long musicianId
  ) {
    MusicianSimpleProfileResponse response = musicianService.getMusicianSimpleProfile(musicianId);
    return ApiResponse.success(response);
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @GetMapping("/me/detail")
  public ApiResponse<MusicianProfileResponse> getMyProfile(
      @CurrentUserId Long musicianId
  ) {
    return ApiResponse.success(musicianService.getMusicianProfile(musicianId));
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @PatchMapping("/me/detail")
  public ApiResponse<Void> updateMyProfile(
      @CurrentUserId Long musicianId,
      @Valid @RequestBody UpdateMusicianProfileRequest request
  ) {
    musicianService.updateMyProfile(musicianId, request);
    return ApiResponse.success();
  }

  @GetMapping("/nickname/check")
  public ApiResponse<Void> checkNickname(@RequestParam String nickname) {
    musicianService.isNicknameAvailable(nickname);
    return ApiResponse.success();
  }

  @GetMapping("/phone/check")
  public ApiResponse<Void> checkPhone(@RequestParam String phone) {
    musicianService.isPhoneAvailable(phone);
    return ApiResponse.success();
  }
}
