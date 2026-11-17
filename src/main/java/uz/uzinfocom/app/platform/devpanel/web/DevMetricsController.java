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
                + "events: 'system', 'http' and 'runtime' (periodic CPU/RAM/disk, per-endpoint and "
                + "JVM/DB-pool/Tomcat-thread snapshots), 'health' (full actuator health tree, slower tick), "
                + "'error' (pushed the moment a new row lands in dev_error_log, see Dev Panel - "
                + "Errors) and 'logger' (a logger level was changed through Dev Panel - Actuator). "
                + "There is no plain request/response fallback - subscribe to the stream. For a focused "
                + "live view of any single actuator operation use GET /v1/dev/actuator-stream/{id} "
                + "(Dev Panel - Actuator).\n\n"
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
                + "- 'runtime': a single RuntimeMetricsResponse JSON OBJECT, e.g. "
                + "{\"uptimeSeconds\":3600.5,\"threadsLive\":84,\"threadsPeak\":96,\"threadsDaemon\":71,"
                + "\"nonHeapMemoryUsedBytes\":1.4e8,\"classesLoaded\":19500,\"gcPauseCount\":42,"
                + "\"gcPauseTotalMs\":310.0,\"openFileDescriptors\":210,\"maxFileDescriptors\":65536,"
                + "\"dbPoolActive\":2,\"dbPoolIdle\":8,\"dbPoolPending\":0,\"dbPoolMax\":10,"
                + "\"tomcatThreadsBusy\":3,\"tomcatThreadsCurrent\":20,\"tomcatThreadsMax\":200,"
                + "\"logErrorEvents\":5,\"logWarnEvents\":120} (any field can be null if its meter isn't "
                + "registered; a dbPoolPending above 0 means requests are queuing for a connection).\n"
                + "- 'health': the actuator health tree as a JSON OBJECT, e.g. "
                + "{\"status\":\"UP\",\"components\":{\"db\":{\"status\":\"UP\",\"details\":{...}},"
                + "\"diskSpace\":{...}}} - full per-component details, unlike the public /v1/actuator/health.\n"
                + "- 'logger': {\"name\":\"uz.uzinfocom.app\",\"configuredLevel\":\"DEBUG\","
                + "\"changedBy\":\"admin\"} - pushed once when a logger level is changed via "
                + "POST /v1/dev/actuator/loggers/{name} (configuredLevel null = reset).\n"
                + "- 'error': a single DevErrorResponse JSON OBJECT (same shape as one row of "
                + "GET /v1/dev/errors), pushed once per new error row, not on a timer.\n\n"
                + "Cadence: 'system', 'http' and 'runtime' are emitted together on every tick of "
                + "app.dev.metrics-stream-interval-ms (default 3000ms, see application.properties) - "
                + "but only while at least one client is connected; 'health' ticks on its own, slower "
                + "app.dev.health-stream-interval-ms (default 10000ms) because it probes the database; "
                + "'error' and 'logger' are event-driven, fired immediately when the change happens, "
                + "independent of those timers."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevMetricsController {

    private final DevSseBroadcaster devSseBroadcaster;

    @Operation(
            summary = "Поток метрик и ошибок (SSE)",
            description = "Server-Sent Events поток, живущий до разрыва соединения клиентом (без таймаута). "
                    + "Эмитит события 'system', 'http' и 'runtime' (периодические снимки CPU/RAM/диска, метрик "
                    + "по эндпоинтам и JVM/пула БД/потоков Tomcat), 'health' (полное дерево health), 'error' "
                    + "(новая запись в dev_error_log) и 'logger' (смена уровня логгера). Кнопка 'Try it out' в Swagger UI "
                    + "здесь не подходит для проверки - запрос не завершится, пока соединение открыто; "
                    + "подключайтесь к потоку напрямую (curl -N, EventSource и т. п.)."
    )
    @GetMapping(ApiPaths.Dev.METRICS_STREAM)
    public SseEmitter stream() {
        return devSseBroadcaster.register();
    }
}
