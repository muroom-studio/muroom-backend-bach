package kr.muroom.muroombackendbach.withdrawal.application;

import java.util.List;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.WithdrawalReason;
import kr.muroom.muroombackendbach.withdrawal.domain.repository.WithdrawalReasonRepository;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.WithdrawalReasonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawalReasonService {

  private final WithdrawalReasonRepository withdrawalReasonRepository;

  public List<WithdrawalReasonResponse> getAllWithdrawalReason() {
    List<WithdrawalReason> withdrawalReasons =
        withdrawalReasonRepository.findAll();

    return withdrawalReasons.stream()
        .map(reason -> WithdrawalReasonResponse.builder()
            .id(reason.getId())
            .code(reason.getCode())
            .description(reason.getDescription())
            .build()
        )
        .toList();
  }

}
