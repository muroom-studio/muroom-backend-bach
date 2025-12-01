package kr.muroom.muroombackendbach.studio.domain.repository;

import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioViewLog;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioViewLogRepository extends JpaRepository<StudioViewLog, Long> {

  boolean findByStudioAndMusicianAndViewedAtAfter(Musician musicianProxy, Studio studio,
      OffsetDateTime startOfTodayUtc);

  boolean findByAnonymousUserIdAndStudioAndViewedAtAfter(String anonymousUserId, Studio studio,
      OffsetDateTime startOfTodayUtc);
}
