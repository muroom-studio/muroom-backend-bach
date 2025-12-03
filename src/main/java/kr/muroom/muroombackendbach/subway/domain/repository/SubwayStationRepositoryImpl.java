package kr.muroom.muroombackendbach.subway.domain.repository;

import static kr.muroom.muroombackendbach.subway.domain.entity.QSubwayStation.subwayStation;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.muroom.muroombackendbach.subway.presentation.dto.StationWithDistance;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;

@RequiredArgsConstructor
public class SubwayStationRepositoryImpl implements SubwayStationQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<StationWithDistance> findNearbyStationsWithDistance(Point point, int radius) {

    // ST_DWithin 함수 (특정 반경 내에 있는지 확인)
    BooleanTemplate dwithin = Expressions.booleanTemplate(
        "ST_DWithin({0}, {1}, {2})",
        subwayStation.location,
        point,
        radius
    );

    // ST_Distance 함수 (두 지점 사이의 거리 계산)
    NumberExpression<Double> distance = Expressions.numberTemplate(
        Double.class,
        "ST_Distance({0}, {1})",
        subwayStation.location,
        point
    );

    return queryFactory
        .select(Projections.constructor(StationWithDistance.class,
            subwayStation,
            distance.as("distance")
        ))
        .from(subwayStation)
        .where(dwithin.eq(true))
        .orderBy(distance.asc()) // 가까운 순으로 정렬
        .fetch();
  }
}