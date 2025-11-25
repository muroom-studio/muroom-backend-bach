package kr.muroom.muroombackendbach.subway.domain.repository;

import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationNearbyStudio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubwayStationsNearbyStudioRepository extends
    JpaRepository<SubwayStationNearbyStudio, Long>, SubwayStationsNearbyStudioQueryRepository {

}
