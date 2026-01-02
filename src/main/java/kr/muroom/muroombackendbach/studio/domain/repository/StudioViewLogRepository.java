package kr.muroom.muroombackendbach.studio.domain.repository;

import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioViewLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioViewLogRepository extends JpaRepository<StudioViewLog, Long> {

  boolean existsByStudioAndMusicianAndViewedAtAfter(Studio studio, Musician musicianProxy,
      OffsetDateTime startOfTodayUtc);

  boolean existsByAnonymousUserIdAndStudioAndViewedAtAfter(String anonymousUserId, Studio studio,
      OffsetDateTime startOfTodayUtc);
}
