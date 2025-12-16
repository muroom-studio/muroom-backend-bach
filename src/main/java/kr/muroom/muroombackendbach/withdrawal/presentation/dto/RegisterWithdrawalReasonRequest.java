package kr.muroom.muroombackendbach.withdrawal.presentation.dto;

import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.MusicianWithdrawal;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.WithdrawalReason;

public record RegisterWithdrawalReasonRequest(
    Long withdrawalReasonId,
    String opinion
) {

  public static MusicianWithdrawal toEntity(
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
