package kr.muroom.muroombackendbach.withdrawal.presentation.dto;

import kr.muroom.muroombackendbach.withdrawal.domain.entity.WithdrawalReasonCode;
import lombok.Builder;

@Builder
public record WithdrawalReasonResponse(
    Long id,
    WithdrawalReasonCode code,
    String description
) {

}
