package kr.muroom.muroombackendbach.subway.domain.repository;

import static kr.muroom.muroombackendbach.subway.domain.entity.QSubwayStation.subwayStation;
import static kr.muroom.muroombackendbach.subway.domain.entity.QSubwayStationNearbyStudio.subwayStationNearbyStudio;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationNearbyStudio;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubwayStationNearbyStudioRepositoryImpl implements SubwayStationNearbyStudioQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<SubwayStationNearbyStudio> findAllByStudioIdInWithStation(Collection<Long> studioIds) {
    return queryFactory
        .select(subwayStationNearbyStudio)
        .from(subwayStationNearbyStudio)
        .join(subwayStationNearbyStudio.subwayStation).fetchJoin()
        .where(subwayStationNearbyStudio.studioId.in(studioIds))
        .fetch();
  }

  @Override
  public List<SubwayStationNearbyStudio> findAllByStudioOrderBySequenceAsc(Long studioId) {
    return queryFactory
        .selectFrom(subwayStationNearbyStudio)
        .join(subwayStationNearbyStudio.subwayStation, subwayStation).fetchJoin()
        .where(subwayStationNearbyStudio.studioId.eq(studioId))
        .orderBy(subwayStationNearbyStudio.sequence.asc())
        .fetch();
  }

  @Override
  public SubwayStationNearbyStudio findFirstByStudioIdOrderBySequenceAsc(Long studioId) {
    return queryFactory
        .selectFrom(subwayStationNearbyStudio)
        .join(subwayStationNearbyStudio.subwayStation, subwayStation).fetchJoin()
        .where(subwayStationNearbyStudio.studioId.eq(studioId))
        .orderBy(subwayStationNearbyStudio.sequence.asc())
        .fetchFirst();
  }
}
