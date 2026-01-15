package kr.muroom.muroombackendbach.owner.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateOwnerProfileRequest(

    @Schema(
        description = "닉네임 (변경 시에만 전달)",
        example = "뮤루뮤루"
    )
    @Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다.")
    String nickname,

    @Schema(description = "연락처 검증 완료 후, 토큰")
    String smsVerifyToken
) {

}
