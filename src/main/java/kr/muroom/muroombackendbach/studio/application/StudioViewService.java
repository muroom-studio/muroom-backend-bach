package kr.muroom.muroombackendbach.studio.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import kr.muroom.muroombackendbach.common.context.AnonymousUserContext;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import kr.muroom.muroombackendbach.musician.domain.repository.MusicianRepository;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioViewLog;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioViewLogRepository;
import kr.muroom.muroombackendbach.studio.exception.StudioErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudioViewService {

  private final StudioViewLogRepository studioViewLogRepository;
  private final StudioRepository studioRepository;
  private final MusicianRepository musicianRepository;

  // TODO: 캐시 반영 고려
  @Async
  @Transactional
  public void incrementViewCount(Long studioId, Long musicianId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new BusinessException(StudioErrorCode.STUDIO_NOT_FOUND));

    boolean alreadyViewedToday;
    OffsetDateTime startOfTodayUtc = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().atStartOfDay()
        .atOffset(ZoneOffset.UTC);

    Musician musicianProxy = null;
    String anonymousUserId = null;

    if (musicianId != null) {
      musicianProxy = musicianRepository.getReferenceById(musicianId);
      alreadyViewedToday = studioViewLogRepository.existsByStudioAndMusicianAndViewedAtAfter(
          studio, musicianProxy, startOfTodayUtc);
    } else {
      anonymousUserId = AnonymousUserContext.getAnonymousUserId();

      if (anonymousUserId == null || anonymousUserId.isBlank()) {
        log.warn("[# incrementViewCount] Anonymous user ID is missing. StudioId: {}", studioId);
        return;
      }

      alreadyViewedToday = studioViewLogRepository.existsByAnonymousUserIdAndStudioAndViewedAtAfter(
          anonymousUserId, studio, startOfTodayUtc);
    }

    if (!alreadyViewedToday) {
      StudioViewLog studioViewLog;
      if (musicianId != null) {
        studioViewLog = StudioViewLog.byMusician(musicianProxy, studio);
      } else {
        studioViewLog = StudioViewLog.byAnonymousUser(anonymousUserId, studio);
      }
      studioViewLogRepository.save(studioViewLog);
    }
  }
}
