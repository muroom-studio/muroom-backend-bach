package kr.muroom.muroombackendbach.admin.withdrawal.presentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.admin.withdrawal.presentation.dto.request.RegisterWithdrawalReasonRequest;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.withdrawal.application.WithdrawalReasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자용 탈퇴 사유 코드 추가")
@RestController
@RequestMapping("/api/admin/withdrawal/reasons")
@RequiredArgsConstructor
public class AdminWithdrawalReasonController {

  private final WithdrawalReasonService withdrawalReasonService;

  @PostMapping
  public ApiResponse<Void> registerWithdrawalReason(
      @Validated @RequestBody RegisterWithdrawalReasonRequest request) {
    withdrawalReasonService.registerWithdrawalReason(request);
    return ApiResponse.success();
  }

}
