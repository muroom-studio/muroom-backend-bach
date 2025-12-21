package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioBoastCommentRepository extends JpaRepository<StudioBoastComment, Long> {

  long countByStudioBoastId(Long studioBoastId);
}
