package kr.muroom.muroombackendbach.subway.domain.repository;

import java.util.Collection;
import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationNearbyStudio;

public interface SubwayStationsNearbyStudioQueryRepository {

  List<SubwayStationNearbyStudio> findAllByStudioIdInWithStation(Collection<Long> studioIds);

  List<SubwayStationNearbyStudio> findAllByStudioOrderBySequenceAsc(Studio studio);

  SubwayStationNearbyStudio findFirstByStudioIdOrderBySequenceAsc(Long studioId);
}
