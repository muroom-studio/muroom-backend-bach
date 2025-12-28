package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import static kr.muroom.muroombackendbach.studioboasting.domain.entity.QStudioBoastComment.studioBoastComment;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StudioBoastCommentRepositoryImpl implements StudioBoastCommentQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Map<Long, Long> findCommentCountsByStudioBoastIn(List<StudioBoast> studioBoasts) {
    if (studioBoasts == null || studioBoasts.isEmpty()) {
      return Map.of();
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
        .collect(Collectors.toMap(
            tuple -> tuple.get(studioBoastComment.studioBoast.id),
            tuple -> {
              Long count = tuple.get(studioBoastComment.count());
              return count != null ? count : 0L;
            }
        ));
  }

}
