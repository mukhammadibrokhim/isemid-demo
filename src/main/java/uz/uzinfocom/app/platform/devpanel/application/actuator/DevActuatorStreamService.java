package uz.uzinfocom.app.platform.devpanel.application.actuator;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Turns any {@code GET} actuator operation into a live Server-Sent-Events feed: the operation is
 * re-invoked every {@code intervalMs} on a small shared scheduler and each answer is pushed as an
 * {@code actuator} event. This is the "watch this one thing closely" channel (one metric, the
 * thread dump, a logger); the dashboard-wide live data (system, http, runtime, health, error) keeps
 * riding the single multiplexed {@code /v1/dev/metrics/stream} connection.
 *
 * <p>Each feed is capped and clamped so a forgotten tab cannot hurt the app: at most
 * {@link #MAX_STREAMS} feeds at once, an interval of {@value #MIN_INTERVAL_MS}ms-{@value #MAX_INTERVAL_MS}ms
 * ({@value #HEAVY_MIN_INTERVAL_MS}ms minimum for expensive endpoints) and a one-hour lifetime after
 * which the panel simply reconnects.
 */
@Slf4j
@Component
public class DevActuatorStreamService {

    public static final int MAX_STREAMS = 20;
    public static final long DEFAULT_INTERVAL_MS = 3_000;
    static final long MIN_INTERVAL_MS = 1_000;
    static final long MAX_INTERVAL_MS = 60_000;
    static final long HEAVY_MIN_INTERVAL_MS = 5_000;
    private static final long EMITTER_TIMEOUT_MS = 60 * 60 * 1000L;

    /** Walk the whole context / dump every thread / render every meter on each tick. */
    private static final Set<String> HEAVY_ENDPOINTS = Set.of(
            "threaddump", "beans", "conditions", "mappings", "env", "configprops", "prometheus", "liquibase"
    );

    private final DevActuatorInvoker invoker;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger open = new AtomicInteger();

    public DevActuatorStreamService(DevActuatorInvoker invoker) {
        this.invoker = invoker;
        AtomicInteger threadCounter = new AtomicInteger();
        this.scheduler = Executors.newScheduledThreadPool(4, runnable -> {
            Thread thread = new Thread(runnable, "dev-actuator-stream-" + threadCounter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }

    public int openStreams() {
        return open.get();
    }

    /**
     * @param onlyChanges push only when the answer differs from the previous one (a bare SSE comment is
     *                    sent otherwise, so dead connections are still noticed)
     * @throws uz.uzinfocom.app.shared.exception.NotFoundException when the endpoint/operation does not exist
     *                                                              (checked with a first, synchronous sample)
     * @throws DevActuatorException                                 when too many feeds are open or the arguments are invalid
     */
    public SseEmitter open(
            String endpointId,
            String subPath,
            MultiValueMap<String, String> query,
            Long requestedIntervalMs,
            boolean onlyChanges,
            SecurityContext securityContext
    ) {
        long intervalMs = clampInterval(endpointId, requestedIntervalMs);

        if (open.incrementAndGet() > MAX_STREAMS) {
            open.decrementAndGet();
            throw DevActuatorException.tooManyStreams(MAX_STREAMS);
        }

        // First sample runs on the request thread so a bad endpoint/argument fails as a normal JSON error
        // instead of a stream that opens and immediately dies.
        DevActuatorResult first;
        try {
            first = invoker.get(endpointId, subPath, query, List.of(), securityContext);
        } catch (RuntimeException rejected) {
            open.decrementAndGet();
            throw rejected;
        }

        Feed feed = new Feed(endpointId, subPath, query, onlyChanges, securityContext);
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        feed.emitter = emitter;

        Runnable stop = () -> {
            if (feed.closed.compareAndSet(false, true)) {
                open.decrementAndGet();
                if (feed.future != null) {
                    feed.future.cancel(false);
                }
            }
        };
        emitter.onCompletion(stop);
        emitter.onTimeout(() -> {
            stop.run();
            emitter.complete();
        });
        emitter.onError(throwable -> stop.run());

        if (!feed.push(first)) {
            stop.run();
            return emitter;
        }
        feed.future = scheduler.scheduleWithFixedDelay(() -> tick(feed, stop), intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        return emitter;
    }

    long clampInterval(String endpointId, Long requested) {
        long interval = requested == null ? DEFAULT_INTERVAL_MS : requested;
        long min = HEAVY_ENDPOINTS.contains(endpointId.toLowerCase(Locale.ROOT))
                ? HEAVY_MIN_INTERVAL_MS
                : MIN_INTERVAL_MS;
        return Math.max(min, Math.min(MAX_INTERVAL_MS, interval));
    }

    private void tick(Feed feed, Runnable stop) {
        if (feed.closed.get()) {
            return;
        }
        try {
            DevActuatorResult result = invoker.get(
                    feed.endpointId, feed.subPath, feed.query, List.of(), feed.securityContext);
            if (!feed.push(result)) {
                stop.run();
            }
        } catch (RuntimeException ex) {
            // A failing sample (metric vanished, actuator hiccup) must not kill the feed - report it
            // and keep going; the client decides whether to give up.
            log.debug("Dev actuator stream sample failed for {}", feed.endpointId, ex);
            if (!feed.pushError(ex)) {
                stop.run();
            }
        }
    }

    private final class Feed {
        final String endpointId;
        final String subPath;
        final MultiValueMap<String, String> query;
        final boolean onlyChanges;
        final SecurityContext securityContext;
        final AtomicBoolean closed = new AtomicBoolean();
        volatile SseEmitter emitter;
        volatile ScheduledFuture<?> future;
        private Object lastBody;
        private boolean sentOnce;

        Feed(String endpointId, String subPath, MultiValueMap<String, String> query,
             boolean onlyChanges, SecurityContext securityContext) {
            this.endpointId = endpointId;
            this.subPath = subPath;
            this.query = query;
            this.onlyChanges = onlyChanges;
            this.securityContext = securityContext;
        }

        /** @return {@code false} when the client is gone and the feed should stop */
        boolean push(DevActuatorResult result) {
            try {
                if (onlyChanges && sentOnce && Objects.equals(lastBody, result.body())) {
                    emitter.send(SseEmitter.event().comment("keepalive"));
                    return true;
                }
                sentOnce = true;
                lastBody = result.body();
                emitter.send(SseEmitter.event()
                        .name("actuator")
                        .data(new DevActuatorEvent(endpointId, subPath, result.status(), Instant.now(), result.body()),
                                MediaType.APPLICATION_JSON));
                return true;
            } catch (IOException | IllegalStateException gone) {
                return false;
            }
        }

        boolean pushError(RuntimeException failure) {
            try {
                emitter.send(SseEmitter.event()
                        .name("actuator-error")
                        .data(new DevActuatorErrorEvent(endpointId, subPath, Instant.now(), failure.getMessage()),
                                MediaType.APPLICATION_JSON));
                return true;
            } catch (IOException | IllegalStateException gone) {
                return false;
            }
        }
    }

    /** Payload of the {@code actuator} SSE event. {@code body} is JSON for JSON operations, a string for text ones. */
    public record DevActuatorEvent(String endpoint, String path, int status, Instant timestamp, Object body) {
    }

    /** Payload of the {@code actuator-error} SSE event. */
    public record DevActuatorErrorEvent(String endpoint, String path, Instant timestamp, String message) {
    }
}
