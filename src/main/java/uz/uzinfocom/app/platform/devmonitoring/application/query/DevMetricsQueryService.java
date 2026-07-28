package uz.uzinfocom.app.platform.devmonitoring.application.query;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.HttpEndpointMetricsResponse;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.SystemMetricsResponse;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Reads CPU/RAM/disk and per-endpoint HTTP metrics directly from the app's
 * existing {@link MeterRegistry} - Spring Boot Actuator already auto-registers
 * all of these (see {@code /v1/actuator/prometheus}), this just reshapes them
 * into a simpler JSON view for the dev-monitoring panel instead of standing up
 * a separate Grafana/Prometheus-server deployment.
 */
@Service
@RequiredArgsConstructor
public class DevMetricsQueryService {

    private final MeterRegistry meterRegistry;

    public SystemMetricsResponse systemSnapshot() {
        return new SystemMetricsResponse(
                gaugeValue("system.cpu.usage", null),
                gaugeValue("process.cpu.usage", null),
                gaugeValue("jvm.memory.used", "heap"),
                gaugeValue("jvm.memory.max", "heap"),
                gaugeValue("disk.free", null),
                gaugeValue("disk.total", null)
        );
    }

    public List<HttpEndpointMetricsResponse> httpSnapshot() {
        record Key(String method, String uri) {
        }

        Map<Key, long[]> counts = new LinkedHashMap<>();
        Map<Key, Timer> representativeTimers = new LinkedHashMap<>();

        for (Meter meter : meterRegistry.find("http.server.requests").meters()) {
            if (!(meter instanceof Timer timer)) {
                continue;
            }

            String method = meter.getId().getTag("method");
            String uri = meter.getId().getTag("uri");
            String status = meter.getId().getTag("status");
            Key key = new Key(method, uri);

            long[] tally = counts.computeIfAbsent(key, ignored -> new long[2]);
            tally[0] += timer.count();
            if (status != null && (status.startsWith("4") || status.startsWith("5"))) {
                tally[1] += timer.count();
            }

            representativeTimers.merge(key, timer,
                    (existing, incoming) -> incoming.max(TimeUnit.MILLISECONDS) > existing.max(TimeUnit.MILLISECONDS)
                            ? incoming
                            : existing);
        }

        return counts.entrySet().stream()
                .map(entry -> {
                    Key key = entry.getKey();
                    long[] tally = entry.getValue();
                    Timer representative = representativeTimers.get(key);
                    return new HttpEndpointMetricsResponse(
                            key.method(),
                            key.uri(),
                            tally[0],
                            tally[1],
                            representative == null ? 0.0 : representative.mean(TimeUnit.MILLISECONDS),
                            representative == null ? 0.0 : representative.max(TimeUnit.MILLISECONDS)
                    );
                })
                .sorted(Comparator.comparingLong(HttpEndpointMetricsResponse::totalCount).reversed())
                .toList();
    }

    private Double gaugeValue(String name, String areaTag) {
        Gauge gauge = areaTag == null
                ? meterRegistry.find(name).gauge()
                : meterRegistry.find(name).tag("area", areaTag).gauge();

        if (gauge == null) {
            return null;
        }

        double value = gauge.value();
        return Double.isNaN(value) ? null : value;
    }
}
