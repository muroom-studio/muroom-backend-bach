package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import java.util.Optional;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastComment;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioBoastCommentLikeRepository extends JpaRepository<StudioBoastCommentLike, Long>,
    StudioBoastCommentLikeQueryRepository {

  Optional<StudioBoastCommentLike> findByMusicianIdAndComment(Long musicianId, StudioBoastComment comment);
}
