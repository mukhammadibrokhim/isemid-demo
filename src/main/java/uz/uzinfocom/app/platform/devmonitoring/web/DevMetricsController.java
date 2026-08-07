package uz.uzinfocom.app.platform.devmonitoring.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uz.uzinfocom.app.platform.devmonitoring.application.stream.DevSseBroadcaster;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

@Tag(
        name = "Dev Monitoring - Metrics",
        description = "Single multiplexed SSE connection for the dev panel, fed from the app's existing "
                + "MeterRegistry (the same data already exposed at /v1/actuator/prometheus). Emits named "
                + "events: 'system' and 'http' (periodic CPU/RAM/disk and per-endpoint metrics snapshots) "
                + "and 'error' (pushed the moment a new row lands in dev_error_log, see Dev Monitoring - "
                + "Errors). There is no plain request/response fallback - subscribe to the stream."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevMetricsController {

    private final DevSseBroadcaster devSseBroadcaster;

    @GetMapping(ApiPaths.Dev.METRICS_STREAM)
    public SseEmitter stream() {
        return devSseBroadcaster.register();
    }
}
