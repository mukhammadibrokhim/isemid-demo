package uz.uzinfocom.app.platform.devpanel.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uz.uzinfocom.app.platform.devpanel.application.stream.DevSseBroadcaster;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

@Tag(
        name = "Dev Panel - Metrics",
        description = "Single multiplexed SSE connection for the dev panel, fed from the app's existing "
                + "MeterRegistry (the same data already exposed at /v1/actuator/prometheus). Emits named "
                + "events: 'system' and 'http' (periodic CPU/RAM/disk and per-endpoint metrics snapshots) "
                + "and 'error' (pushed the moment a new row lands in dev_error_log, see Dev Panel - "
                + "Errors). There is no plain request/response fallback - subscribe to the stream.\n\n"
                + "Event shapes (see DevMetricsStreamPublisher / DevErrorLogWriter for the emitting code):\n"
                + "- 'system': a single SystemMetricsResponse JSON OBJECT, e.g. "
                + "{\"systemCpuUsage\":0.34,\"processCpuUsage\":0.12,\"heapMemoryUsedBytes\":1.2e8,"
                + "\"heapMemoryMaxBytes\":5.0e8,\"diskFreeBytes\":2.1e10,\"diskTotalBytes\":5.0e10} "
                + "(any field can be null if the underlying MeterRegistry gauge isn't registered).\n"
                + "- 'http': a JSON ARRAY of HttpEndpointMetricsResponse, one entry per distinct "
                + "method+uri seen so far, e.g. [{\"method\":\"GET\",\"uri\":\"/v1/dev/errors\","
                + "\"totalCount\":42,\"count2xx\":40,\"count3xx\":0,\"count4xx\":2,\"count5xx\":0,"
                + "\"errorCount\":2,\"meanDurationMs\":12.4,\"maxDurationMs\":88.1,\"p50DurationMs\":9.0,"
                + "\"p95DurationMs\":40.2,\"p99DurationMs\":80.5}, ...] - never a single object.\n"
                + "- 'error': a single DevErrorResponse JSON OBJECT (same shape as one row of "
                + "GET /v1/dev/errors), pushed once per new error row, not on a timer.\n\n"
                + "Cadence: 'system' and 'http' are emitted together on every tick of "
                + "app.dev.metrics-stream-interval-ms (default 3000ms, see application.properties) - "
                + "but only while at least one client is connected; 'error' is event-driven, fired "
                + "immediately when DevErrorLogWriter persists a new dev_error_log row, independent of "
                + "that timer."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevMetricsController {

    private final DevSseBroadcaster devSseBroadcaster;

    @Operation(
            summary = "Поток метрик и ошибок (SSE)",
            description = "Server-Sent Events поток, живущий до разрыва соединения клиентом (без таймаута). "
                    + "Эмитит события 'system' и 'http' (периодические снимки CPU/RAM/диска и метрик по "
                    + "эндпоинтам) и 'error' (новая запись в dev_error_log). Кнопка 'Try it out' в Swagger UI "
                    + "здесь не подходит для проверки - запрос не завершится, пока соединение открыто; "
                    + "подключайтесь к потоку напрямую (curl -N, EventSource и т. п.)."
    )
    @GetMapping(ApiPaths.Dev.METRICS_STREAM)
    public SseEmitter stream() {
        return devSseBroadcaster.register();
    }
}
