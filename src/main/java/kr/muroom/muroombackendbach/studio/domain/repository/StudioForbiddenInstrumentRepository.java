package kr.muroom.muroombackendbach.studio.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioForbiddenInstrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudioForbiddenInstrumentRepository extends JpaRepository<StudioForbiddenInstrument, Long> {

  List<StudioForbiddenInstrument> findAllByStudio(Studio studio);

  void deleteAllByStudio(Studio studio);
}