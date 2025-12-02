package kr.muroom.muroombackendbach.subway.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationNearbyStudio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubwayStationsNearbyStudioRepository extends
    JpaRepository<SubwayStationNearbyStudio, Long>, SubwayStationsNearbyStudioQueryRepository {

  List<SubwayStationNearbyStudio> findAllByStudioOrderBySequenceAsc(Studio studio);
}
