package kr.muroom.muroombackendbach.studioboasting.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.Builder;

@Schema(description = "작업실 소개(자랑) 댓글 응답 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record StudioBoastCommentResponse(
    @Schema(description = "댓글 ID", example = "12345678901234")
    String id,

    @Schema(description = "댓글 내용", example = "이 작업실 정말 멋지네요!")
    String content,

    @Schema(description = "댓글 작성일시", example = "2023-10-05T14:48:00Z")
    OffsetDateTime createdAt,

    @Schema(description = "댓글이 비밀 댓글인지 여부", example = "false")
    boolean isSecret,

    @Schema(description = "댓글이 삭제된 댓글인지 여부", example = "false")
    boolean isDeleted,

    @Schema(description = "현 사용자에게 댓글이 보이는지 여부", example = "true")
    boolean isVisible,

    @Schema(description = "댓글 작성자 정보")
    CreatorUserInfo creatorUserInfo,

    @Schema(description = "태그된 사용자 정보", nullable = true)
    TaggedUserInfo taggedUserInfo,

    @Schema(description = "요청한 사용자가 작성한 댓글인지 여부")
    boolean isWrittenByRequestUser,

    @Schema(description = "좋아요 여부", example = "true")
    boolean isLikedByRequestUser,

    @Schema(description = "대댓글 목록")
    List<StudioBoastCommentReplyResponse> replies,

    @Schema(description = "좋아요 수", example = "42")
    Long likeCount
) {

  @Schema(description = "댓글 작성자 정보")
  public record CreatorUserInfo(
      @Schema(description = "작성자 뮤지션 ID", example = "12345678901234")
      String id,

      @Schema(description = "작성자 닉네임", example = "뮤지션닉네임")
      String nickname
  ) {

    public static CreatorUserInfo from(Musician creator) {
      if (creator == null) {
        return new CreatorUserInfo(null, "탈퇴한 사용자");
      }
      return new CreatorUserInfo(creator.getId().toString(), creator.getNickname());
    }
  }

  @Schema(description = "태그된 사용자 정보")
  public record TaggedUserInfo(
      @Schema(description = "태그된 뮤지션 ID", example = "12345678901234")
      String id,

      @Schema(description = "태그된 뮤지션 닉네임", example = "태그된뮤지션닉네임")
      String nickname
  ) {

    public static TaggedUserInfo from(Musician taggedUser) {
      if (taggedUser == null) {
        return new TaggedUserInfo(null, "탈퇴한 사용자");
      }
      return new TaggedUserInfo(taggedUser.getId().toString(), taggedUser.getNickname());
    }
  }
}
