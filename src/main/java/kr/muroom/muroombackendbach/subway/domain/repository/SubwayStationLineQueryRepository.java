package kr.muroom.muroombackendbach.subway.domain.repository;

import java.util.Collection;
import java.util.List;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStation;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationLine;

public interface SubwayStationLineQueryRepository {

  List<SubwayStationLine> findAllByStudioIdsInWithLine(Collection<Long> studioIds);

  List<SubwayStationLine> findAllByStation(SubwayStation station);
}
