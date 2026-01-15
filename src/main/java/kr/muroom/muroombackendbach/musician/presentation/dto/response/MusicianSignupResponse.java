package kr.muroom.muroombackendbach.musician.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MusicianSignupResponse(
    @Schema(example = "791543436721219205")
    String musicianId
) {

}
