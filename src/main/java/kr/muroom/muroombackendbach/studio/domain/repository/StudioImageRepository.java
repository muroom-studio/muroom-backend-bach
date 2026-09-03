package kr.muroom.muroombackendbach.studio.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioImageRepository extends JpaRepository<StudioImage, Long> {

  List<StudioImage> findAllByStudio(Studio studio);

  void deleteAllByStudio(Studio studio);
}
