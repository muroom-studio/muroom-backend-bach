package kr.muroom.muroombackendbach.subway.domain.repository;

import static kr.muroom.muroombackendbach.subway.domain.entity.QSubwayStationLine.subwayStationLine;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationLine;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubwayStationLineRepositoryImpl implements SubwayStationLineQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<SubwayStationLine> findAllByStudioIdsWithLine(Collection<Long> studioIds) {
    return queryFactory
        .select(subwayStationLine)
        .from(subwayStationLine)
        .join(subwayStationLine.line).fetchJoin()
        .where(subwayStationLine.station.id.in(studioIds))
        .fetch();
  }
}
