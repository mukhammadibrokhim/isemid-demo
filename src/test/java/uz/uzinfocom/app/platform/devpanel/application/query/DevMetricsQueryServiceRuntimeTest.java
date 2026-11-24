package uz.uzinfocom.app.platform.devpanel.application.query;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.RuntimeMetricsResponse;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DevMetricsQueryServiceRuntimeTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final DevMetricsQueryService service = new DevMetricsQueryService(registry);

    @Test
    void sumsMultiInstanceMetersAndLeavesUnregisteredOnesNull() {
        Gauge.builder("jvm.threads.live", () -> 84).register(registry);
        Gauge.builder("jvm.memory.used", () -> 100).tags("area", "nonheap", "id", "Metaspace").register(registry);
        Gauge.builder("jvm.memory.used", () -> 30).tags("area", "nonheap", "id", "CodeCache").register(registry);
        Gauge.builder("jvm.memory.used", () -> 999).tags("area", "heap", "id", "G1 Eden").register(registry);
        Gauge.builder("hikaricp.connections.active", () -> 3).tag("pool", "main").register(registry);
        Gauge.builder("hikaricp.connections.active", () -> 2).tag("pool", "reports").register(registry);
        Gauge.builder("hikaricp.connections.pending", () -> 5).tag("pool", "main").register(registry);
        registry.timer("jvm.gc.pause", "action", "end of minor GC").record(40, TimeUnit.MILLISECONDS);
        registry.timer("jvm.gc.pause", "action", "end of major GC").record(60, TimeUnit.MILLISECONDS);
        registry.counter("logback.events", "level", "error").increment(4);
        registry.counter("logback.events", "level", "warn").increment(9);
        registry.counter("logback.events", "level", "info").increment(1000);

        RuntimeMetricsResponse runtime = service.runtimeSnapshot();

        assertThat(runtime.threadsLive()).isEqualTo(84.0);
        assertThat(runtime.nonHeapMemoryUsedBytes()).isEqualTo(130.0);
        assertThat(runtime.dbPoolActive()).isEqualTo(5.0);
        assertThat(runtime.dbPoolPending()).isEqualTo(5.0);
        assertThat(runtime.gcPauseCount()).isEqualTo(2L);
        assertThat(runtime.gcPauseTotalMs()).isEqualTo(100.0);
        assertThat(runtime.logErrorEvents()).isEqualTo(4.0);
        assertThat(runtime.logWarnEvents()).isEqualTo(9.0);

        assertThat(runtime.tomcatThreadsBusy()).isNull();
        assertThat(runtime.dbPoolMax()).isNull();
        assertThat(runtime.classesLoaded()).isNull();
    }

    @Test
    void anEmptyRegistryYieldsAllNulls() {
        RuntimeMetricsResponse runtime = service.runtimeSnapshot();

        assertThat(runtime.threadsLive()).isNull();
        assertThat(runtime.gcPauseCount()).isNull();
        assertThat(runtime.gcPauseTotalMs()).isNull();
        assertThat(runtime.logErrorEvents()).isNull();
        assertThat(runtime.dbPoolActive()).isNull();
    }
}
