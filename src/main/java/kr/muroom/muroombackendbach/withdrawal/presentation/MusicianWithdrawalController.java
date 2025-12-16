package kr.muroom.muroombackendbach.withdrawal.presentation;

import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.withdrawal.application.MusicianWithdrawalService;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.RegisterWithdrawalReasonRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/withdrawal/musicians")
@RequiredArgsConstructor
public class MusicianWithdrawalController {

  private final MusicianWithdrawalService musicianWithdrawalService;

  @PostMapping
  public ApiResponse<Void> register(@RequestBody RegisterWithdrawalReasonRequest request) {
    musicianWithdrawalService.register(request);
    return ApiResponse.success();
  }
}
