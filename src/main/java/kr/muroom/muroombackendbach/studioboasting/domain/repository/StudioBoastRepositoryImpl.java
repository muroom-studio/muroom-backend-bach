package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import static kr.muroom.muroombackendbach.studioboasting.domain.entity.QStudioBoast.studioBoast;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class StudioBoastRepositoryImpl implements StudioBoastQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<StudioBoast> findAllRandomly(Pageable pageable) {
    List<StudioBoast> results = queryFactory
        .selectFrom(studioBoast)
        .orderBy(Expressions.numberTemplate(Double.class, "RANDOM()").asc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(studioBoast.count())
        .from(studioBoast);

    Long total = countQuery.fetchOne();
    if (total == null) {
      total = 0L;
    }

    return new PageImpl<>(results, pageable, total);
  }
}
