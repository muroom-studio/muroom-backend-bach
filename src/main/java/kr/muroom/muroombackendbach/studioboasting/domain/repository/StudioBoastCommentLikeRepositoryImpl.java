package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import static kr.muroom.muroombackendbach.studioboasting.domain.entity.QStudioBoastCommentLike.studioBoastCommentLike;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastComment;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StudioBoastCommentLikeRepositoryImpl implements StudioBoastCommentLikeQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Set<Long> findLikedCommentIdsByCreatorUserIdAndCommentIn(Long musicianId, List<StudioBoastComment> comments) {
    if (comments == null || comments.isEmpty()) {
      return Set.of();
    }

    List<Long> likedCommentIds = queryFactory
        .select(studioBoastCommentLike.comment.id)
        .from(studioBoastCommentLike)
        .where(
            studioBoastCommentLike.musicianId.eq(musicianId),
            studioBoastCommentLike.comment.in(comments)
        )
        .fetch();

    return Set.copyOf(likedCommentIds);
  }

  @Override
  public Map<Long, Long> findLikeCountsByCommentIn(List<StudioBoastComment> comments) {
    if (comments == null || comments.isEmpty()) {
      return Map.of();
    }

    return queryFactory
        .select(studioBoastCommentLike.comment.id, studioBoastCommentLike.count())
        .from(studioBoastCommentLike)
        .where(studioBoastCommentLike.comment.in(comments))
        .groupBy(studioBoastCommentLike.comment.id)
        .fetch()
        .stream()
        .collect(
            Collectors.toMap(
                tuple -> tuple.get(studioBoastCommentLike.comment.id),
                tuple -> {
                  Long count = tuple.get(studioBoastCommentLike.count());
                  return count != null ? count : 0L;
                }
            )
        );
  }

  @Override
  public void deleteAllByStudioBoast(StudioBoast studioBoast) {
    queryFactory
        .delete(studioBoastCommentLike)
        .where(studioBoastCommentLike.comment.studioBoast.eq(studioBoast))
        .execute();
  }
}
