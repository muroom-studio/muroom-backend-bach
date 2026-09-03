package kr.muroom.muroombackendbach.withdrawal.job;

import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.withdrawal.application.HardDeleteCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HardDeleteScheduler {

  @Value("${withdrawal.hard-delete.batch-size:10}")
  private int batchSize;
  @Value("${withdrawal.hard-delete.max-loop:200}")
  private int maxLoop;

  private final HardDeleteCleanupService cleanupService;

  @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
  public void runDaily() {
    OffsetDateTime now = OffsetDateTime.now();

    // 조금씩 여러번 횟수로 반복
    for (int i = 0; i < maxLoop; i++) {
      int deleted = cleanupService.cleanupMusicians(now, batchSize);
      if (deleted == 0) {
        break;
      }
    }
  }
}
