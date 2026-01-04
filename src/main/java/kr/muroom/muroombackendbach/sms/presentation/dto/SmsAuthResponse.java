package kr.muroom.muroombackendbach.sms.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "SmsAuthResponse",
    description = "SMS 인증번호 요청에 대한 응답. 오늘 남은 인증번호 전송 가능 횟수를 포함합니다."
)
public record SmsAuthResponse(
    @Schema(example = "3")
    int remainingDailySendCount
) {

}
