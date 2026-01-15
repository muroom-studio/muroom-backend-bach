package kr.muroom.muroombackendbach.musician.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateMusicianProfileRequest(

    @Schema(
        description = "닉네임 (변경 시에만 전달)",
        example = "뮤루뮤루"
    )
    @Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다.")
    String nickname,

    @Schema(
        description = "악기 ID (변경 시에만 전달)",
        example = "791543436721219205"
    )
    Long instrumentId,

    String smsVerifyToken,

    @Schema(example = "뮤룸 스튜디오")
    @Size(max = 255, message = "작업실 이름은 최대 255자까지 가능합니다.")
    String studioName,

    @Schema(example = "서울 관악구 남부순환로118길 12 (봉천동, 와르르맨션Ⅱ)")
    @Size(max = 255, message = "도로명 주소는 최대 255자까지 가능합니다.")
    String roadAddress,

    @Schema(example = "619호")
    @Size(max = 255, message = "상세 주소는 최대 255자까지 가능합니다.")
    String detailAddress
) {

}
