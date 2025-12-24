package kr.muroom.muroombackendbach.user.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MusicianSignupResponse(
    @Schema(example = "asdfhal.skdfjwei9rasdcmxz.12sasdw")
    String accessToken,
    @Schema(example = "대충 토큰 값")
    String refreshToken,
    @Schema(example = "1")
    String musicianId
) {

}
