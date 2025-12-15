package kr.muroom.muroombackendbach.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record UpdateMusicianProfileRequest(

    @Schema(
        description = "닉네임 (변경 시에만 전달)",
        example = "뮤루뮤루"
    )
    String nickname,

    @Schema(
        description = "악기 ID (변경 시에만 전달)",
        example = "1"
    )
    Long instrumentId,

    @Schema(example = "010-1234-6071", description = "변경할 전화번호")
    String phone,

    @Schema(example = "뮤룸 스튜디오")
    String studioName,

    String roadAddress,

    String detailAddress
) {

}
