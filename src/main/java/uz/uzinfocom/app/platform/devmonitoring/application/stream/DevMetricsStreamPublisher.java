package uz.uzinfocom.app.platform.devmonitoring.application.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.devmonitoring.application.query.DevMetricsQueryService;

/**
 * Periodically pushes {@link DevMetricsQueryService} snapshots as
 * {@code system}/{@code http} events on the shared {@link DevSseBroadcaster}
 * stream. Skips the snapshot work entirely when no dev panel tab is
 * connected, so this never runs against an empty room.
 */
@Component
@RequiredArgsConstructor
public class DevMetricsStreamPublisher {

    private final DevMetricsQueryService devMetricsQueryService;
    private final DevSseBroadcaster devSseBroadcaster;

    @Scheduled(fixedRateString = "${app.dev.metrics-stream-interval-ms:3000}")
    public void publish() {
        if (!devSseBroadcaster.hasSubscribers()) {
            return;
        }

        devSseBroadcaster.broadcast("system", devMetricsQueryService.systemSnapshot());
        devSseBroadcaster.broadcast("http", devMetricsQueryService.httpSnapshot());
    }
}
