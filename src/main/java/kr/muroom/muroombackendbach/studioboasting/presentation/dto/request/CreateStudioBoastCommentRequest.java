package kr.muroom.muroombackendbach.studioboasting.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Schema(description = "작업실 자랑 댓글 생성 요청 DTO")
@Builder
public record CreateStudioBoastCommentRequest(
    @Schema(description = "댓글 내용", example = "작업실 너무 예쁘네요!", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 1000)
    String content,

    @Schema(description = "비밀 댓글 여부", example = "true", requiredMode = RequiredMode.REQUIRED)
    Boolean isSecret,

    @Schema(description = "대댓글일 경우, 부모 댓글의 ID", nullable = true)
    Long parentId,

    @Schema(description = "태그할 사용자의 ID", nullable = true)
    Long taggedUserId
) {

}
