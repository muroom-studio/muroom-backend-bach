package kr.muroom.muroombackendbach.studio.job;

import kr.muroom.muroombackendbach.studio.application.command.StudioDraftCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudioDraftExpirationScheduler {

  private final StudioDraftCommandService studioDraftCommandService;

  @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
  public void deleteExpiredDrafts() {
    studioDraftCommandService.deleteExpiredDrafts();
  }
}
