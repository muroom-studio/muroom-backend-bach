package kr.muroom.muroombackendbach.withdrawal.presentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.withdrawal.application.MusicianWithdrawalService;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.RegisterWithdrawalReasonRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/withdrawal/musicians")
@RequiredArgsConstructor
@Tag(name = "뮤지션 탈퇴 API", description = "로그인된 사용자만 요청 가능 합니다.")
public class MusicianWithdrawalController {

  private final MusicianWithdrawalService musicianWithdrawalService;

  @PostMapping
  public ApiResponse<Void> register(
      @AuthenticationPrincipal Long musicianId,
      @RequestBody RegisterWithdrawalReasonRequest request) {
    musicianWithdrawalService.register(musicianId, request);
    return ApiResponse.success();
  }
}
