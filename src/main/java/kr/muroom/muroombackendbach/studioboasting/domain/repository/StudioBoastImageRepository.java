package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioBoastImageRepository extends JpaRepository<StudioBoastImage, Long> {

  List<StudioBoastImage> findByStudioBoastIdOrderBySequenceAsc(Long studioBoastId);

  void deleteAllByStudioBoastId(Long studioBoastId);

  List<StudioBoastImage> findAllByStudioBoastId(Long studioBoastId);
}
