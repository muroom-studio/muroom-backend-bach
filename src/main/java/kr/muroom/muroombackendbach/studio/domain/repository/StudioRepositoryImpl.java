package kr.muroom.muroombackendbach.studio.domain.repository;

import static kr.muroom.muroombackendbach.studio.domain.entity.QStudio.studio;
import static kr.muroom.muroombackendbach.studio.domain.entity.QStudioPrice.studioPrice;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class StudioRepositoryImpl implements StudioQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Studio> findStudiosWithinBounds(
      Double minLatitude, Double maxLatitude,
      Double minLongitude, Double maxLongitude
  ) {
    return queryFactory
        .selectFrom(studio)
        .where(
            studio.deletedAt.isNull(),
            isWithinBounds(minLatitude, maxLatitude, minLongitude, maxLongitude)
        )
        .fetch();
  }

  @Override
  public Page<Studio> findStudiosForMapList(
      Double minLatitude, Double maxLatitude,
      Double minLongitude, Double maxLongitude,
      Pageable pageable) {
    List<Studio> content = queryFactory
        .selectFrom(studio)
        .leftJoin(studioPrice).on(studio.id.eq(studioPrice.studio.id))
        .where(
            studio.deletedAt.isNull(),
            isWithinBounds(minLatitude, maxLatitude, minLongitude, maxLongitude)
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(studio.count())
        .from(studio)
        .where(
            studio.deletedAt.isNull(),
            isWithinBounds(minLatitude, maxLatitude, minLongitude, maxLongitude)
        );

    Long totalResult = countQuery.fetchOne();
    long total = totalResult != null ? totalResult : 0L;

    return new PageImpl<>(content, pageable, total);
  }

//  private OrderSpecifier<?> getOrderSpecifier()

  private BooleanExpression isWithinBounds(Double minLatitude, Double maxLatitude,
      Double minLongitude, Double maxLongitude) {

    return Expressions.booleanTemplate(
        "st_intersects({0}, st_makeenvelope({1}, {2}, {3}, {4}, 4326))",
        studio.location,
        Expressions.constant(minLongitude),
        Expressions.constant(minLatitude),
        Expressions.constant(maxLongitude),
        Expressions.constant(maxLatitude)
    );
  }
}
