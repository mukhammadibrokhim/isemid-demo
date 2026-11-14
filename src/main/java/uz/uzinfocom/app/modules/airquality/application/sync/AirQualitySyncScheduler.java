package uz.uzinfocom.app.modules.airquality.application.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.shared.exception.AppException;

/**
 * Periodic AIRCONTROL pull. Present only when {@code app.air-quality.sync.enabled=true}
 * (default {@code false}: it needs credentials and IMUT network access). Every expected
 * failure (not configured, upstream down, circuit open, a manual run in progress) is
 * logged and swallowed so the next tick simply tries again.
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.air-quality.sync.enabled", havingValue = "true")
public class AirQualitySyncScheduler {

    private final AirQualitySyncService syncService;

    @Scheduled(
            initialDelayString = "${app.air-quality.sync.initial-delay-ms:120000}",
            fixedDelayString = "${app.air-quality.sync.interval-ms:3600000}")
    public void run() {
        try {
            syncService.sync();
        } catch (AppException expected) {
            log.warn("event=air_quality_sync_skipped reason={}", expected.getMessage());
        } catch (RuntimeException unexpected) {
            log.error("event=air_quality_sync_failure failureType={}", unexpected.getClass().getName(), unexpected);
        }
    }
}
