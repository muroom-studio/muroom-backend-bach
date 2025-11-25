package kr.muroom.muroombackendbach.subway.domain.repository;

import kr.muroom.muroombackendbach.subway.domain.entity.SubwayStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubwayStationRepository extends JpaRepository<SubwayStation, Long> {

}
