package kr.muroom.muroombackendbach.withdrawal.application;

import static kr.muroom.muroombackendbach.withdrawal.exception.WithdrawalReasonErrorCode.ALREADY_EXIST_WITHDRAWAL_REASON_CODE;

import java.util.List;
import kr.muroom.muroombackendbach.admin.withdrawal.presentation.dto.request.RegisterWithdrawalReasonRequest;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.WithdrawalReason;
import kr.muroom.muroombackendbach.withdrawal.domain.repository.WithdrawalReasonRepository;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.WithdrawalReasonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WithdrawalReasonService {

  private final WithdrawalReasonRepository withdrawalReasonRepository;

  public List<WithdrawalReasonResponse> getAllWithdrawalReason() {
    List<WithdrawalReason> withdrawalReasons =
        withdrawalReasonRepository.findAllByOrderBySequenceOrderAscIdAsc();

    return withdrawalReasons.stream()
        .map(reason -> WithdrawalReasonResponse.builder()
            .id(String.valueOf(reason.getId()))
            .code(reason.getCode())
            .description(reason.getDescription())
            .build()
        )
        .toList();
  }

  @Transactional
  public void registerWithdrawalReason(RegisterWithdrawalReasonRequest request) {
    if (withdrawalReasonRepository.existsByCode(request.code())) {
      throw new BusinessException(ALREADY_EXIST_WITHDRAWAL_REASON_CODE);
    }

    WithdrawalReason withdrawalReason = WithdrawalReason.builder()
        .code(request.code())
        .description(request.description())
        .isActive(request.isActive())
        .build();

    withdrawalReasonRepository.save(withdrawalReason);
  }
}
