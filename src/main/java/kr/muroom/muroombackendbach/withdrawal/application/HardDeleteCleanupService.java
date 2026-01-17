package kr.muroom.muroombackendbach.withdrawal.application;

import java.time.OffsetDateTime;
import java.util.List;
import kr.muroom.muroombackendbach.withdrawal.domain.repository.MusicianHardDeleteDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HardDeleteCleanupService {

  private final MusicianHardDeleteDao dao;

  @Transactional
  public int cleanupMusicians(OffsetDateTime now, int batchSize) {
    List<Long> ids = dao.findTargetMusicianIds(now, batchSize);
    if (ids.isEmpty()) {
      return 0;
    }

    dao.deleteStudioBoastCommentByMusicianIds(ids);
    dao.deleteStudioBoastCommentLikeByMusicianIds(ids);
    return dao.deleteMusicianByMusicianIds(ids);
  }

}
