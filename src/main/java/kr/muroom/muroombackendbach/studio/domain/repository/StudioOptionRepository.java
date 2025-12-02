package kr.muroom.muroombackendbach.studio.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioOptionRepository extends JpaRepository<StudioOption, Long> {

  List<StudioOption> findAllByStudio(Studio studio);
}
