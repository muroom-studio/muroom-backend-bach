package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioBoastImageRepository extends JpaRepository<StudioBoastImage, Long> {

  List<StudioBoastImage> findByStudioBoastOrderBySequenceAsc(StudioBoast studioBoast);

  List<StudioBoastImage> findAllByStudioBoast(StudioBoast studioBoast);

  List<StudioBoastImage> findAllByStudioBoastIn(List<StudioBoast> studioBoasts);

  void deleteAllByStudioBoast(StudioBoast studioBoast);
}
