package kr.muroom.muroombackendbach.admin.withdrawal.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterWithdrawalReasonRequest(
    @Schema(example = "LACK_OF_LISTING_INFO", description = "중복 되지 않는 코드값 입력")
    String code,
    @Schema(example = "매물 정보의 신뢰가 부족함", description = "내용 드롭다운 표시해야함으로 짧게 입력")
    String description,
    @Schema(example = "true", description = "활성화 여부")
    Boolean isActive
) {

}
