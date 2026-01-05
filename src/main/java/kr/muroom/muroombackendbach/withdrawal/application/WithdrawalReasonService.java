package kr.muroom.muroombackendbach.withdrawal.application;

import static kr.muroom.muroombackendbach.withdrawal.exception.WithdrawalReasonErrorCode.ALREADY_EXIST_WITHDRAWAL_REASON_CODE;

import java.util.List;
import kr.muroom.muroombackendbach.admin.withdrawal.presentation.dto.request.RegisterWithdrawalReasonRequest;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.sms.presentation.SmsSender;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.WithdrawalReason;
import kr.muroom.muroombackendbach.withdrawal.domain.repository.WithdrawalReasonRepository;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.WithdrawalAssembler;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.response.WithdrawalReasonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WithdrawalReasonService {

  private final WithdrawalReasonRepository withdrawalReasonRepository;
  private final WithdrawalAssembler withdrawalAssembler;

  public List<WithdrawalReasonResponse> getAllWithdrawalReason() {
    List<WithdrawalReason> withdrawalReasons =
        withdrawalReasonRepository.findAllByOrderBySequenceAscIdAsc();

    return withdrawalReasons.stream()
        .map(withdrawalAssembler::toResponse)
        .toList();
  }

  @Transactional
  public void registerWithdrawalReason(RegisterWithdrawalReasonRequest request) {
    if (withdrawalReasonRepository.existsByCode(request.code())) {
      throw new BusinessException(ALREADY_EXIST_WITHDRAWAL_REASON_CODE);
    }

    WithdrawalReason withdrawalReason = withdrawalAssembler.toRegisterWithdrawalReason(request);
    withdrawalReasonRepository.save(withdrawalReason);
  }
}
