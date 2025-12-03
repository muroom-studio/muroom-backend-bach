package kr.muroom.muroombackendbach.subway.domain.repository;

import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStationLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubwayStationLineRepository extends JpaRepository<SubwayStationLine, Long>, SubwayStationLineQueryRepository {

}
