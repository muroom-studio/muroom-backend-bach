package kr.muroom.muroombackendbach.withdrawal.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.MusicianWithdrawal;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.WithdrawalReason;

public record RegisterMusicianWithdrawalRequest(
    @NotNull
    @Schema(example = "791543436721219205", description = "존재하는 탈퇴 사유 ID를 넣어주세요")
    Long withdrawalReasonId,

    @Schema(example = "서비스가 너무 좋지만, 가야할 것 같아요", description = "null 가능")
    @Size(max = 2000)
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
