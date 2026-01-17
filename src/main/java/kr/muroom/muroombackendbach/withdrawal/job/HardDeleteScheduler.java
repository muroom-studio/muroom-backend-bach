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

  @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
  public void runDaily() {
    OffsetDateTime now = OffsetDateTime.now();
    int batchSize = 500;

    // 한번 실행에 너무 오래 도는 걸 막고 싶으면 maxLoop를 둠
    int maxLoop = 200;

    for (int i = 0; i < maxLoop; i++) {
      int deleted = cleanupService.cleanupMusicians(now, batchSize);
      if (deleted == 0) {
        break;
      }
    }
  }
}
