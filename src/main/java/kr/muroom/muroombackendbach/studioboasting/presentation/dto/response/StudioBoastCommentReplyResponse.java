package kr.muroom.muroombackendbach.studioboasting.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastCommentResponse.CreatorUserInfo;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastCommentResponse.TaggedUserInfo;
import lombok.Builder;

@Schema(description = "작업실 소개(자랑) 대댓글 응답 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record StudioBoastCommentReplyResponse(

    @Schema(description = "댓글 ID", example = "12345678901234")
    String id,

    @Schema(description = "댓글 내용", example = "이 작업실 정말 멋지네요!")
    String content,

    @Schema(description = "댓글 작성일시", example = "2023-10-05T14:48:00Z")
    OffsetDateTime createdAt,

    @Schema(description = "댓글이 비밀 댓글인지 여부", example = "false")
    Boolean isSecret,

    @Schema(description = "댓글이 삭제된 댓글인지 여부", example = "false")
    Boolean isDeleted,

    @Schema(description = "현 사용자에게 댓글이 보이는지 여부", example = "true")
    Boolean isVisible,

    @Schema(description = "댓글 작성자 정보")
    CreatorUserInfo creatorUserInfo,

    @Schema(description = "태그된 사용자 정보")
    TaggedUserInfo taggedUserInfo,

    @Schema(description = "요청한 사용자가 작성한 댓글인지 여부")
    Boolean isWrittenByRequestUser,

    @Schema(description = "좋아요 여부", example = "true")
    Boolean isLikedByRequestUser,

    @Schema(description = "좋아요 수", example = "42")
    Long likeCount
) {

}
