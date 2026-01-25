package kr.muroom.muroombackendbach.common.listener;

import kr.muroom.muroombackendbach.musician.domain.repository.MusicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarmupListener implements ApplicationRunner {

    private final ApplicationEventPublisher eventPublisher;
    private final MusicianRepository musicianRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("STARTING WARMUP...");

        try {
            // Simple DB Warmup
            long count = musicianRepository.count();
            log.info("Warmup: Musician count = {}", count);
        } catch (Exception e) {
            log.error("Warmup failed", e);
            // We might want to let the app start anyway, or fail it.
            // For now, logging error is enough, but traffic shouldn't be accepted if crucial.
            // However, letting it proceed allows debugging.
        }

        // Explicitly set Readiness State to ACCEPTING_TRAFFIC
        AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);

        log.info("WARMUP FINISHED. Application is now Ready.");
    }
}
