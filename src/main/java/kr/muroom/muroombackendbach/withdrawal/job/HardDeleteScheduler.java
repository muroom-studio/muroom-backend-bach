package kr.muroom.muroombackendbach.withdrawal.job;

import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.withdrawal.application.HardDeleteCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HardDeleteScheduler {

  private final HardDeleteCleanupService cleanupService;
  final static int BATCH_SIZE = 10;
  final static int MAX_LOOP = 200;

  @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
  public void runDaily() {
    OffsetDateTime now = OffsetDateTime.now();

    // 조금씩 여러번 횟수로 반복
    for (int i = 0; i < MAX_LOOP; i++) {
      int deleted = cleanupService.cleanupMusicians(now, BATCH_SIZE);
      if (deleted == 0) {
        break;
      }
    }
  }
}
