package kr.muroom.muroombackendbach.subway.domain.repository;

import kr.muroom.muroombackendbach.subway.domain.entity.SubwayLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubwayLineRepository extends JpaRepository<SubwayLine, Long> {

}
