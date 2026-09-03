package kr.muroom.muroombackendbach.musician.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "닉네임 사용 가능 여부 응답 DTO")
public record MusicianNicknameCheckResponse(

    @Schema(description = "사용 가능 여부", example = "true")
    boolean available
) {

}
