package kr.muroom.muroombackendbach.studio.domain.repository;

import static kr.muroom.muroombackendbach.room.domain.entity.QRoom.room;
import static kr.muroom.muroombackendbach.studio.domain.entity.QStudio.studio;
import static kr.muroom.muroombackendbach.studio.domain.entity.QStudioBuildingInfo.studioBuildingInfo;
import static kr.muroom.muroombackendbach.studio.domain.entity.QStudioForbiddenInstrument.studioForbiddenInstrument;
import static kr.muroom.muroombackendbach.studio.domain.entity.QStudioOption.studioOption;
import static kr.muroom.muroombackendbach.studio.domain.entity.QStudioPrice.studioPrice;
import static kr.muroom.muroombackendbach.subway.domain.entity.QSubwayStation.subwayStation;
import static kr.muroom.muroombackendbach.subway.domain.entity.QSubwayStationNearbyStudio.subwayStationNearbyStudio;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.studio.domain.entity.QStudio;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.domain.enums.OptionCategory;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomType;
import kr.muroom.muroombackendbach.studio.presentation.dto.request.MapSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
@SuppressWarnings({"ClassCanBeRecord", "unused"})
public class StudioRepositoryImpl implements StudioQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Studio> findStudiosWithinBounds(MapSearchRequest request) {
    List<Long> studioIds = queryFactory
        .select(studio.id)
        .from(studio)
        .leftJoin(studio.studioBuildingInfo, studioBuildingInfo)
        .leftJoin(studio.studioPrice, studioPrice)
        .leftJoin(studio.rooms, room)
        .where(
            studio.deletedAt.isNull(),
            studioFilteringWhereClause(request)
        )
        .groupBy(studio.id)
        .fetch();

    if (studioIds.isEmpty()) {
      return new ArrayList<>();
    }

    return queryFactory
        .selectFrom(studio).distinct()
        .leftJoin(studio.rooms, room).fetchJoin()
        .leftJoin(studio.studioPrice, studioPrice).fetchJoin()
        .where(studio.id.in(studioIds))
        .fetch();
  }

  @Override
  public Page<Studio> findStudiosForMapList(MapSearchRequest request, Pageable pageable) {
    // 정렬 및 페이징을 적용하여 스튜디오 ID 목록을 먼저 조회
    JPAQuery<Long> idsQuery = queryFactory
        .select(studio.id)
        .from(studio)
        .leftJoin(studio.studioBuildingInfo, studioBuildingInfo)
        .leftJoin(studio.studioPrice, studioPrice)
        .leftJoin(studio.rooms, room);

    idsQuery.where(
            studio.deletedAt.isNull(),
            studioFilteringWhereClause(request)
        )
        .groupBy(studio.id, studioPrice.minPrice)
        .orderBy(studioOrderSpecifiers(pageable))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize());

    List<Long> studioIds = idsQuery.fetch();

    if (studioIds.isEmpty()) {
      return Page.empty(pageable);
    }

    // 조회된 ID 목록을 기반으로 실제 스튜디오 엔티티들을 다시 조회
    List<Studio> content = queryFactory
        .selectFrom(studio).distinct()
        .leftJoin(studio.rooms, room).fetchJoin()
        .leftJoin(studio.studioPrice, studioPrice).fetchJoin()
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
        .leftJoin(studio.studioBuildingInfo, studioBuildingInfo)
        .leftJoin(studio.studioPrice, studioPrice)
        .leftJoin(studio.rooms, room)
        .where(
            studio.deletedAt.isNull(),
            studioFilteringWhereClause(request)
        );

    Long totalResult = countQuery.fetchOne();
    long total = totalResult != null ? totalResult : 0L;

    return new PageImpl<>(sortedContent, pageable, total);
  }

  private BooleanBuilder studioFilteringWhereClause(MapSearchRequest request) {
    BooleanBuilder whereClause = new BooleanBuilder();

    whereClause.and(matchKeyword(request.keyword()));
    whereClause.and(
        isWithinBounds(request.minLatitude(), request.maxLatitude(),
            request.minLongitude(), request.maxLongitude()));
    whereClause.and(matchPriceRange(request.minPrice(), request.maxPrice()));
    whereClause.and(hasMatchingRoomSize(request.minRoomWidth(), request.maxRoomWidth(),
        request.minRoomHeight(), request.maxRoomHeight()));
    whereClause.and(hasAllOptionsInCategory(request.commonOptionCodes(), OptionCategory.COMMON));
    whereClause.and(
        hasAllOptionsInCategory(request.individualOptionCodes(), OptionCategory.INDIVIDUAL));
    whereClause.and(inFloorTypes(request.floorTypes()));
    whereClause.and(inRestroomTypes(request.restroomTypes()));
    whereClause.and(isParkingAvailable(request.isParkingAvailable()));
    whereClause.and(isLodgingAvailable(request.isLodgingAvailable()));
    whereClause.and(hasFireInsurance(request.hasFireInsurance()));
    whereClause.and(notForbidsInstruments(request.forbiddenInstrumentCodes()));

    return whereClause;
  }

  private OrderSpecifier<?>[] studioOrderSpecifiers(Pageable pageable) {
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
            NumberExpression<Integer> minRoomPrice = room.basePrice.min();
            // coalesce를 사용하여 Room 가격이 없을 경우 Studio의 최소 가격을 사용
            NumberExpression<Integer> effectiveMinPrice = minRoomPrice.coalesce(
                studioPrice.minPrice);
            orderSpecifiers.add(new OrderSpecifier<>(direction, effectiveMinPrice,
                OrderSpecifier.NullHandling.NullsLast));
            break;
          default:
            break;
        }
      }
    }

    // 정렬 조건이 없거나, 유효하지 않은 경우 기본 정렬(최신순) 적용
    if (orderSpecifiers.isEmpty()) {
      orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, studio.createdAt));
    }

    return orderSpecifiers.toArray(new OrderSpecifier[0]);
  }

  private BooleanExpression matchKeyword(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return null;
    }

    // 조건 1: 스튜디오명에 키워드가 포함되는 경우
    BooleanExpression studioNameMatches = studio.name.containsIgnoreCase(keyword);

    // 조건 2: 인증 지하철역명에 키워드가 포함되는 경우
    BooleanExpression studioLinkedToStation = JPAExpressions.selectOne()
        .from(subwayStationNearbyStudio)
        .join(subwayStationNearbyStudio.subwayStation, subwayStation)
        .where(
            subwayStationNearbyStudio.studio.eq(studio),
            subwayStation.name.containsIgnoreCase(keyword)
        ).exists();

    return studioNameMatches.or(studioLinkedToStation);
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

  private BooleanExpression matchPriceRange(Integer minPrice, Integer maxPrice) {
    if (minPrice == null && maxPrice == null) {
      return null;
    }

    BooleanBuilder studioPriceMatches = new BooleanBuilder();
    studioPriceMatches.and(studioPrice.minPrice.isNotNull());
    studioPriceMatches.and(studioPrice.maxPrice.isNotNull());

    if (minPrice != null) {
      studioPriceMatches.and(studioPrice.maxPrice.goe(minPrice));
    }
    if (maxPrice != null) {
      studioPriceMatches.and(studioPrice.minPrice.loe(maxPrice));
    }

    // 조건 1: 가격 범위에 맞는 Room이 하나라도 존재하는 스튜디오
    BooleanExpression hasRoomWithMatchingPrice = JPAExpressions.selectOne()
        .from(room)
        .where(
            room.studio.eq(studio),
            room.basePrice.isNotNull(),
            between(room.basePrice, minPrice, maxPrice)
        ).exists();

    // 조건 2: Room에는 가격 정보가 전혀 없지만, Studio 자체의 가격이 범위에 맞는 스튜디오
    BooleanExpression noRoomPriceExists = JPAExpressions.selectOne()
        .from(room)
        .where(
            room.studio.eq(studio),
            room.basePrice.isNotNull()
        ).notExists();

    return hasRoomWithMatchingPrice.or(noRoomPriceExists.and(studioPriceMatches));
  }

  private BooleanExpression hasMatchingRoomSize(Integer minWidth, Integer maxWidth,
      Integer minHeight, Integer maxHeight) {
    if (minWidth == null && maxWidth == null && minHeight == null && maxHeight == null) {
      return null;
    }
    return JPAExpressions.selectOne()
        .from(room)
        .where(
            room.studio.eq(studio), // 메인 쿼리의 studio와 현재 서브쿼리의 room을 연결
            between(room.width, minWidth, maxWidth),   // width 조건
            between(room.height, minHeight, maxHeight) // height 조건
        ).exists();
  }

  private BooleanExpression hasAllOptionsInCategory(List<String> optionCodes,
      OptionCategory optionCategory) {
    if (CollectionUtils.isEmpty(optionCodes)) {
      return null;
    }
    return studio.id.in(JPAExpressions.select(studioOption.studio.id)
        .from(studioOption)
        .where(studioOption.option.category.eq(optionCategory)
            .and(studioOption.option.code.in(optionCodes)))
        .groupBy(studioOption.studio.id)
        .having(studioOption.option.code.count().eq((long) optionCodes.size()))
    );
  }

  private BooleanExpression inFloorTypes(List<FloorType> floorTypes) {
    if (CollectionUtils.isEmpty(floorTypes)) {
      return null;
    }
    return studio.studioBuildingInfo.floorType.in(floorTypes);
  }

  private BooleanExpression isLodgingAvailable(Boolean isLodgingAvailable) {
    if (isLodgingAvailable == null) {
      return null;
    }
    return studio.studioBuildingInfo.isLodgingAvailable.eq(isLodgingAvailable);
  }

  private BooleanExpression inRestroomTypes(List<RestroomType> restroomTypes) {
    if (CollectionUtils.isEmpty(restroomTypes)) {
      return null;
    }
    return studio.studioBuildingInfo.restroomType.in(restroomTypes);
  }

  private BooleanExpression isParkingAvailable(Boolean isParkingAvailable) {
    if (isParkingAvailable == null) {
      return null;
    }
    return studio.studioBuildingInfo.isParkingAvailable.eq(isParkingAvailable);
  }

  private BooleanExpression hasFireInsurance(Boolean hasFireInsurance) {
    if (hasFireInsurance == null) {
      return null;
    }
    return studio.studioBuildingInfo.hasFireInsurance.eq(hasFireInsurance);
  }

  private BooleanExpression notForbidsInstruments(List<String> forbiddenInstrumentCodes) {
    if (CollectionUtils.isEmpty(forbiddenInstrumentCodes)) {
      return null;
    }
    return studio.id.notIn(JPAExpressions.select(studioForbiddenInstrument.studio.id)
        .from(studioForbiddenInstrument)
        .where(studioForbiddenInstrument.instrument.code.in(forbiddenInstrumentCodes))
    );
  }

  /**
   * 숫자 범위 조건을 생성하는 범용 헬퍼 메소드입니다.
   */
  private <T extends Number & Comparable<?>> BooleanExpression between(NumberPath<T> path, T min,
      T max) {
    if (min == null && max == null) {
      return null;
    }
    if (min != null && max != null) {
      return path.between(min, max);
    }
    return min != null ? path.goe(min) : path.loe(max);
  }
}
