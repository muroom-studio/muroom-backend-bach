package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import java.util.List;
import java.util.Optional;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastLike;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioBoastLikeRepository extends JpaRepository<StudioBoastLike, Long> {

  Optional<StudioBoastLike> findByMusicianAndStudioBoast(Musician musician, StudioBoast studioBoast); // 객체로 조회

  boolean existsByMusicianAndStudioBoast(Musician musician, StudioBoast studioBoast); // 객체로 조회

  List<StudioBoastLike> findAllByMusicianAndStudioBoastIn(Musician musician, List<StudioBoast> studioBoasts); // 내가 좋아요한 게시글 목록 조회

  void deleteAllByStudioBoast(StudioBoast studioBoast); // StudioBoast 객체로 삭제

  long countByStudioBoast(StudioBoast studioBoast); // StudioBoast 객체로 카운트
}