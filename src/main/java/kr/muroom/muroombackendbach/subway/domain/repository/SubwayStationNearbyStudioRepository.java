package kr.muroom.muroombackendbach.subway.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationNearbyStudio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubwayStationNearbyStudioRepository extends
    JpaRepository<SubwayStationNearbyStudio, Long>, SubwayStationNearbyStudioQueryRepository {

  List<SubwayStationNearbyStudio> findAllByStudioIdOrderBySequenceAsc(Long studioId);

  void deleteAllByStudioId(Long studioId);
}
