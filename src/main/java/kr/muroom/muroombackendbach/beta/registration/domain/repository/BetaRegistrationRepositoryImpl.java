package kr.muroom.muroombackendbach.beta.registration.domain.repository;

import static kr.muroom.muroombackendbach.beta.registration.domain.entity.QBetaIntroductoryImage.betaIntroductoryImage;
import static kr.muroom.muroombackendbach.beta.registration.domain.entity.QBetaRegistration.betaRegistration;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.muroom.muroombackendbach.beta.registration.domain.entity.BetaRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class BetaRegistrationRepositoryImpl implements BetaRegistrationQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<BetaRegistration> findAllWithImages(Pageable pageable) {
    // 1단계: 페이지네이션을 적용하여 ID 목록 조회
    List<Long> ids = queryFactory
        .select(betaRegistration.id)
        .from(betaRegistration)
        .orderBy(getOrderSpecifiers(pageable.getSort())) // 동적 정렬 적용
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // 조회된 ID가 없으면 빈 페이지 반환
    if (ids.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, 0);
    }

    // 2단계: 조회된 ID를 기반으로 컬렉션을 포함한 전체 데이터 조회
    // (정렬 순서를 유지하기 위해 orderBy를 다시 적용해야 함)
    List<BetaRegistration> content = queryFactory
        .selectFrom(betaRegistration).distinct()
        .leftJoin(betaRegistration.introductoryImages, betaIntroductoryImage).fetchJoin()
        .where(betaRegistration.id.in(ids))
        .orderBy(getOrderSpecifiers(pageable.getSort())) // 동적 정렬 적용
        .fetch();

    // Count 쿼리 (별도로 실행)
    Long counts = queryFactory
        .select(betaRegistration.id.count())
        .from(betaRegistration)
        .fetchOne();
    long total = counts == null ? 0 : counts;

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public Long countDistinctPhoneNumber() {
    return queryFactory
        .select(betaRegistration.phoneNumber.countDistinct())
        .from(betaRegistration)
        .fetchOne();
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
    return sort.stream()
        .map(order -> {
          Order direction = order.isAscending() ? Order.ASC : Order.DESC;
          String property = order.getProperty();

          // BetaRegistration 엔티티의 경로를 생성
          PathBuilder<BetaRegistration> pathBuilder = new PathBuilder<>(BetaRegistration.class,
              "betaRegistration");

          // 경로에서 정렬할 속성을 찾아 OrderSpecifier 생성
          return new OrderSpecifier<>(direction, pathBuilder.get(property, Comparable.class));
        })
        .toArray(OrderSpecifier[]::new);
  }
}
