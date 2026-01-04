package kr.muroom.muroombackendbach.withdrawal.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.withdrawal.application.WithdrawalReasonService;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.WithdrawalReasonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/withdrawal-reasons")
@RequiredArgsConstructor
@Tag(name = "musician withdrawal reason - 탈퇴 사유 API")
public class WithdrawalReasonController {

  private final WithdrawalReasonService withdrawalReasonService;

  @Operation(
      summary = "탈퇴 사유 전체 조회",
      description = "회원 탈퇴 시 사용자에게 제공되는 모든 탈퇴 사유 목록을 조회합니다."
  )
  @GetMapping
  public ApiResponse<List<WithdrawalReasonResponse>> getAllWithdrawalReason() {
    return ApiResponse.success(withdrawalReasonService.getAllWithdrawalReason());
  }
}
