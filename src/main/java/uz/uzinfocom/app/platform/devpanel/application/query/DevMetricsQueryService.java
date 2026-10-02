package uz.uzinfocom.app.platform.devpanel.application.query;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.HttpEndpointMetricsResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.SystemMetricsResponse;

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

        Map<Key, HttpTally> tallies = new LinkedHashMap<>();

        for (Meter meter : meterRegistry.find("http.server.requests").meters()) {
            if (!(meter instanceof Timer timer)) {
                continue;
            }

            String method = meter.getId().getTag("method");
            String uri = meter.getId().getTag("uri");
            String status = meter.getId().getTag("status");
            Key key = new Key(method, uri);

            tallies.computeIfAbsent(key, ignored -> new HttpTally()).accumulate(status, timer);
        }

        return tallies.entrySet().stream()
                .map(entry -> {
                    Key key = entry.getKey();
                    HttpTally tally = entry.getValue();
                    Timer representative = tally.representative;
                    double[] percentiles = percentiles(representative);
                    return new HttpEndpointMetricsResponse(
                            key.method(),
                            key.uri(),
                            tally.totalCount(),
                            tally.count2xx,
                            tally.count3xx,
                            tally.count4xx,
                            tally.count5xx,
                            tally.errorCount(),
                            representative == null ? 0.0 : representative.mean(TimeUnit.MILLISECONDS),
                            tally.maxDurationMs,
                            percentiles[0],
                            percentiles[1],
                            percentiles[2]
                    );
                })
                .sorted(Comparator.comparingLong(HttpEndpointMetricsResponse::totalCount).reversed())
                .toList();
    }

    /**
     * Percentiles can't be summed across the per-status-code {@link Timer} instances Micrometer
     * keeps for a single method+uri, so they're read off the busiest one (by request count) as a
     * representative sample instead of an exact aggregate.
     */
    private double[] percentiles(Timer representative) {
        double[] result = {0.0, 0.0, 0.0};
        if (representative == null) {
            return result;
        }

        for (ValueAtPercentile value : representative.takeSnapshot().percentileValues()) {
            double ms = value.value(TimeUnit.MILLISECONDS);
            if (Math.abs(value.percentile() - 0.5) < 0.001) {
                result[0] = ms;
            } else if (Math.abs(value.percentile() - 0.95) < 0.001) {
                result[1] = ms;
            } else if (Math.abs(value.percentile() - 0.99) < 0.001) {
                result[2] = ms;
            }
        }
        return result;
    }

    /**
     * Per method+uri accumulator across all status-code variants of the
     * {@code http.server.requests} timer.
     */
    private static final class HttpTally {
        long count2xx;
        long count3xx;
        long count4xx;
        long count5xx;
        double maxDurationMs;
        Timer representative;
        long representativeCount = -1;

        void accumulate(String status, Timer timer) {
            long count = timer.count();
            char statusClass = status != null && !status.isEmpty() ? status.charAt(0) : ' ';
            switch (statusClass) {
                case '2' -> count2xx += count;
                case '3' -> count3xx += count;
                case '4' -> count4xx += count;
                case '5' -> count5xx += count;
                default -> {
                }
            }

            maxDurationMs = Math.max(maxDurationMs, timer.max(TimeUnit.MILLISECONDS));
            if (count > representativeCount) {
                representativeCount = count;
                representative = timer;
            }
        }

        long totalCount() {
            return count2xx + count3xx + count4xx + count5xx;
        }

        long errorCount() {
            return count4xx + count5xx;
        }
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
