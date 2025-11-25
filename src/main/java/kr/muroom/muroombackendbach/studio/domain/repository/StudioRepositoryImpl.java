package kr.muroom.muroombackendbach.studio.domain.repository;

import static kr.muroom.muroombackendbach.studio.domain.entity.QStudio.studio;
import static kr.muroom.muroombackendbach.studio.domain.entity.QStudioPrice.studioPrice;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.studio.domain.entity.QStudio;
import kr.muroom.muroombackendbach.studio.domain.entity.QStudioPrice;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
@SuppressWarnings({"ClassCanBeRecord", "unused"})
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
    // 정렬 및 페이징을 적용하여 스튜디오 ID 목록을 먼저 조회
    List<Long> studioIds = queryFactory
        .select(studio.id)
        .from(studio)
        .leftJoin(studioPrice).on(studio.id.eq(studioPrice.studio.id))
        .where(
            studio.deletedAt.isNull(),
            isWithinBounds(minLatitude, maxLatitude, minLongitude, maxLongitude)
        )
        .orderBy(getOrderSpecifiers(pageable))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    if (studioIds.isEmpty()) {
      return Page.empty(pageable);
    }

    // 조회된 ID 목록을 기반으로 실제 스튜디오 엔티티들을 다시 조회
    List<Studio> content = queryFactory
        .selectFrom(studio)
        .where(studio.id.in(studioIds))
        .fetch();

    // 조회된 스튜디오들을 ID 순서에 맞게 정렬
    Map<Long, Studio> contentMap = content.stream()
        .collect(Collectors.toMap(Studio::getId, Function.identity()));
    List<Studio> sortedContent = studioIds.stream()
        .map(contentMap::get)
        .toList();

    // 전체 카운트를 조회하여 Page 객체 생성
    JPAQuery<Long> countQuery = queryFactory
        .select(studio.count())
        .from(studio)
        .where(
            studio.deletedAt.isNull(),
            isWithinBounds(minLatitude, maxLatitude, minLongitude, maxLongitude)
        );

    Long totalResult = countQuery.fetchOne();
    long total = totalResult != null ? totalResult : 0L;

    return new PageImpl<>(sortedContent, pageable, total);
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

    if (pageable.getSort().isSorted()) {
      for (Sort.Order order : pageable.getSort()) {
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;
        String property = order.getProperty();

        switch (property) {
          case "latest":
            orderSpecifiers.add(new OrderSpecifier<>(direction, QStudio.studio.createdAt));
            break;
          case "price":
            orderSpecifiers.add(new OrderSpecifier<>(direction, QStudioPrice.studioPrice.minPrice,
                OrderSpecifier.NullHandling.NullsLast));
            // 다른 정렬 기준이 필요하면 여기에 case 추가
            break;
          default:
            break;
        }
      }
    }

    // 정렬 조건이 없거나, 유효하지 않은 경우 기본 정렬(최신순) 적용
    if (orderSpecifiers.isEmpty()) {
      orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, QStudio.studio.createdAt));
    }

    return orderSpecifiers.toArray(new OrderSpecifier[0]);
  }

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
