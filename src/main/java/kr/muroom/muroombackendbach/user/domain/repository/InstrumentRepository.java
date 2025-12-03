package kr.muroom.muroombackendbach.user.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.user.domain.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

  List<Instrument> findAllByCodeIn(List<String> instrumentCodes);
}
