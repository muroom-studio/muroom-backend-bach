package kr.muroom.muroombackendbach.withdrawal.presentation;

import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.withdrawal.application.WithdrawalReasonService;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.WithdrawalReasonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/withdrawal/reasons")
@RequiredArgsConstructor
public class WithdrawalReasonController {

  private final WithdrawalReasonService withdrawalReasonService;

  @GetMapping
  public ApiResponse<List<WithdrawalReasonResponse>> getAllWithdrawalReason() {
    return ApiResponse.success(withdrawalReasonService.getAllWithdrawalReason());
  }
}
