package kr.muroom.muroombackendbach.withdrawal.presentation;

import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.config.CurrentUserId;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.withdrawal.application.MusicianWithdrawalService;
import kr.muroom.muroombackendbach.withdrawal.presentation.docs.MusicianWithdrawalControllerDocs;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.request.RegisterMusicianWithdrawalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/withdrawal/musician")
@RequiredArgsConstructor
public class MusicianWithdrawalController implements MusicianWithdrawalControllerDocs {

  private final MusicianWithdrawalService musicianWithdrawalService;

  @PreAuthorize("hasRole('MUSICIAN')")
  @PostMapping
  public ApiResponse<Void> register(
      @CurrentUserId Long musicianId,
      @Valid @RequestBody RegisterMusicianWithdrawalRequest request) {
    musicianWithdrawalService.register(musicianId, request);
    return ApiResponse.success();
  }
}
