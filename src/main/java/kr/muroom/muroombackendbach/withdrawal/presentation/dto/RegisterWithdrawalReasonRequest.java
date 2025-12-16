package kr.muroom.muroombackendbach.withdrawal.presentation.dto;

import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.MusicianWithdrawal;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.WithdrawalReason;

public record RegisterWithdrawalReasonRequest(
    Long musicianId,
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
        .createdAt(OffsetDateTime.now())
        .build();
  }
}
