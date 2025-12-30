package kr.muroom.muroombackendbach.withdrawal.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record WithdrawalReasonResponse(
    String id,

    @Schema(example = "LACK_OF_LISTING_INFO")
    String code,

    @Schema(example = "원하는 매물 정보가 부족함")
    String description
) {

}
