package kr.muroom.muroombackendbach.withdrawal.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.WithdrawalReasonCode;
import lombok.Builder;

@Builder
public record WithdrawalReasonResponse(
    Long id,
    @Schema(example = "LACK_OF_LISTING_INFO")
    WithdrawalReasonCode code,
    @Schema(example = "원하는 매물 정보가 부족함")
    String description
) {

}
