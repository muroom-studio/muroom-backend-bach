package kr.muroom.muroombackendbach.withdrawal.presentation.dto;

import kr.muroom.muroombackendbach.admin.withdrawal.presentation.dto.request.RegisterWithdrawalReasonRequest;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.MusicianWithdrawal;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.WithdrawalReason;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.response.WithdrawalReasonResponse;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalAssembler {

  public WithdrawalReasonResponse toResponse(WithdrawalReason withdrawalReason) {
    return WithdrawalReasonResponse.builder()
        .id(String.valueOf(withdrawalReason.getId()))
        .code(withdrawalReason.getCode())
        .description(withdrawalReason.getDescription())
        .build();
  }

  public WithdrawalReason toRegisterWithdrawalReason(RegisterWithdrawalReasonRequest request) {
    return WithdrawalReason.builder()
        .code(request.code())
        .isActive(request.isActive())
        .description(request.description())
        .build();
  }

  public MusicianWithdrawal toRegisterMusicianWithdrawal(
      Musician musician,
      WithdrawalReason withdrawalReason,
      String opinion
  ) {
    return MusicianWithdrawal.builder()
        .musician(musician)
        .withdrawalReason(withdrawalReason)
        .opinion(opinion)
        .build();
  }
}
