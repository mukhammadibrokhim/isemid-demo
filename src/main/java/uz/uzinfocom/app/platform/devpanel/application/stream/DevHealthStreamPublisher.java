package uz.uzinfocom.app.platform.devpanel.application.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import uz.uzinfocom.app.platform.devpanel.application.actuator.DevActuatorInvoker;
import uz.uzinfocom.app.platform.devpanel.application.actuator.DevActuatorResult;
import uz.uzinfocom.app.platform.devpanel.application.actuator.DevActuatorSecurityContext;

import java.util.List;

/**
 * Pushes the full actuator health tree (status plus per-component details: db, diskSpace,
 * circuit breakers, ...) as the {@code health} event on the shared {@link DevSseBroadcaster}
 * stream. Slower than the metrics tick because a health check actually probes the database;
 * skipped entirely when no panel tab is connected.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DevHealthStreamPublisher {

    private final DevActuatorInvoker invoker;
    private final DevSseBroadcaster devSseBroadcaster;

    @Scheduled(fixedRateString = "${app.dev.health-stream-interval-ms:10000}")
    public void publish() {
        if (!devSseBroadcaster.hasSubscribers() || !invoker.isAvailable("health")) {
            return;
        }

        try {
            DevActuatorResult health = invoker.get(
                    "health", "", new LinkedMultiValueMap<>(), List.of(), DevActuatorSecurityContext.system());
            devSseBroadcaster.broadcast("health", health.body());
        } catch (RuntimeException ex) {
            log.debug("Dev panel health snapshot failed", ex);
        }
    }
}
