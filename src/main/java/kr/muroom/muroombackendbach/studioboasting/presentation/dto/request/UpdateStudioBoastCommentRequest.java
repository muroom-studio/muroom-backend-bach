package kr.muroom.muroombackendbach.studioboasting.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "작업실 소개(자랑) 댓글 수정 요청 DTO")
public record UpdateStudioBoastCommentRequest(
    @Schema(description = "수정할 댓글 내용", example = "작업실 정말 멋지네요!")
    @NotBlank
    @Size(max = 1000)
    String content,

    @NotNull
    @Schema(description = "비밀댓글 여부", example = "true")
    Boolean isSecret
) {

}