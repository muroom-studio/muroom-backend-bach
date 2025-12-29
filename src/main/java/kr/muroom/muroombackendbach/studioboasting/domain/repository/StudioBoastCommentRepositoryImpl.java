package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import static kr.muroom.muroombackendbach.studioboasting.domain.entity.QStudioBoastComment.studioBoastComment;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StudioBoastCommentRepositoryImpl implements StudioBoastCommentQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Map<Long, Long> countTopLevelCommentsByStudioBoastIn(List<StudioBoast> studioBoasts) {
    if (studioBoasts == null || studioBoasts.isEmpty()) {
      return Collections.emptyMap();
    }

    return queryFactory
        .select(
            studioBoastComment.studioBoast.id,
            studioBoastComment.count()
        )
        .from(studioBoastComment)
        .where(
            studioBoastComment.studioBoast.in(studioBoasts),
            studioBoastComment.parent.isNull()
        )
        .groupBy(studioBoastComment.studioBoast.id)
        .fetch()
        .stream()
        // key(게시글 ID)가 null인 비정상적인 경우는 필터링
        .filter(tuple -> tuple.get(0, Long.class) != null)
        .collect(Collectors.toMap(
            tuple -> tuple.get(0, Long.class),
            // value(댓글 수)가 혹시라도 null일 경우 0L으로 대체
            tuple -> Objects.requireNonNullElse(tuple.get(1, Long.class), 0L)
        ));
  }

  @Override
  public Long countTopLevelCommentsByStudioBoast(StudioBoast studioBoast) {
    return queryFactory
        .select(studioBoastComment.count())
        .from(studioBoastComment)
        .where(
            studioBoastComment.studioBoast.eq(studioBoast),
            studioBoastComment.parent.isNull()
        )
        .fetchOne();
  }
}
