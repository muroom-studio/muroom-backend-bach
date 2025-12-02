package kr.muroom.muroombackendbach.subway.domain.repository;

import static kr.muroom.muroombackendbach.subway.domain.entity.QSubwayStation.subwayStation;
import static kr.muroom.muroombackendbach.subway.domain.entity.QSubwayStationNearbyStudio.subwayStationNearbyStudio;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationNearbyStudio;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubwayStationsNearbyStudioRepositoryImpl implements SubwayStationsNearbyStudioQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<SubwayStationNearbyStudio> findAllByStudioIdInWithStation(Collection<Long> studioIds) {
    return queryFactory
        .select(subwayStationNearbyStudio)
        .from(subwayStationNearbyStudio)
        .join(subwayStationNearbyStudio.subwayStation).fetchJoin()
        .where(subwayStationNearbyStudio.studio.id.in(studioIds))
        .fetch();
  }

  @Override
  public List<SubwayStationNearbyStudio> findAllByStudioOrderBySequenceAsc(Studio studio) {
    return queryFactory
        .selectFrom(subwayStationNearbyStudio)
        .join(subwayStationNearbyStudio.subwayStation, subwayStation).fetchJoin()
        .where(subwayStationNearbyStudio.studio.eq(studio))
        .orderBy(subwayStationNearbyStudio.sequence.asc())
        .fetch();
  }
}
