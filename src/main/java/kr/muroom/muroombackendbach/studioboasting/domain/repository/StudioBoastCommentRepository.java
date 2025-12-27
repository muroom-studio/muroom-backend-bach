package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioBoastCommentRepository extends JpaRepository<StudioBoastComment, Long>, StudioBoastCommentQueryRepository {

  // 최상위 댓글 목록을 '페이지네이션'하여 조회합니다.
  Page<StudioBoastComment> findByStudioBoastAndParentIsNull(StudioBoast studioBoast, Pageable pageable);

  // 여러 부모 댓글에 속한 모든 대댓글 목록을 한 번의 쿼리로 조회합니다 (N+1 방지용).
  List<StudioBoastComment> findByParentInOrderByCreatedAtAsc(List<StudioBoastComment> parents);

  long countByStudioBoast(StudioBoast studioBoast);

  void deleteAllByStudioBoast(StudioBoast studioBoast);
}