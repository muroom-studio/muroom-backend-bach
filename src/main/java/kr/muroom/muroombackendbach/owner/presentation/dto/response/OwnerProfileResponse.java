package kr.muroom.muroombackendbach.owner.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record OwnerProfileResponse(
    @Schema(description = "사장님 ID", example = "791543436721219205")
    String ownerId,

    @Schema(description = "닉네임", example = "뮤루뮤루")
    String nickname,

    @Schema(description = "이름", example = "박근혜")
    String name,

    @Schema(description = "전화번호", example = "010-1234-1234")
    String phone
) {

}
