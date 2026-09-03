package kr.muroom.muroombackendbach.subway.domain.repository;

import java.util.Collection;
import java.util.List;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationLine;

public interface SubwayStationLineQueryRepository {

  List<SubwayStationLine> findAllByStudioIdsInWithLine(Collection<Long> studioIds);

  List<SubwayStationLine> findAllByStationIdsInWithLine(Collection<Long> stationIds);

  List<SubwayStationLine> findAllByStationIdInWithLine(Long stationId);
}
