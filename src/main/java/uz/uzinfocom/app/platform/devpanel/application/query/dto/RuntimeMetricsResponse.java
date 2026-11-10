package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * JVM / connection-pool / servlet-container snapshot pushed as the {@code runtime} event on the
 * dev-panel stream. Every field is nullable: it is {@code null} when the underlying meter is not
 * registered (e.g. {@code tomcatThreads*} only exist with {@code server.tomcat.mbeanregistry.enabled=true}).
 */
@Schema(description = "JVM, DB connection pool and Tomcat thread snapshot (nullable fields = meter not registered).")
public record RuntimeMetricsResponse(
        @Schema(description = "Seconds since the JVM process started.")
        Double uptimeSeconds,

        @Schema(description = "Live JVM threads (daemon + non-daemon).")
        Double threadsLive,
        @Schema(description = "Peak live JVM threads since start.")
        Double threadsPeak,
        @Schema(description = "Live daemon JVM threads.")
        Double threadsDaemon,

        @Schema(description = "Used non-heap memory (metaspace, code cache, ...), bytes.")
        Double nonHeapMemoryUsedBytes,
        @Schema(description = "Loaded classes.")
        Double classesLoaded,

        @Schema(description = "GC pauses since start.")
        Long gcPauseCount,
        @Schema(description = "Total GC pause time since start, milliseconds.")
        Double gcPauseTotalMs,

        @Schema(description = "Open file descriptors.")
        Double openFileDescriptors,
        @Schema(description = "File descriptor limit.")
        Double maxFileDescriptors,

        @Schema(description = "DB pool connections in use (summed over pools).")
        Double dbPoolActive,
        @Schema(description = "DB pool idle connections.")
        Double dbPoolIdle,
        @Schema(description = "Threads waiting for a DB connection - above 0 means the pool is saturated.")
        Double dbPoolPending,
        @Schema(description = "DB pool maximum size.")
        Double dbPoolMax,

        @Schema(description = "Tomcat request threads currently busy.")
        Double tomcatThreadsBusy,
        @Schema(description = "Tomcat request threads currently alive.")
        Double tomcatThreadsCurrent,
        @Schema(description = "Tomcat request thread limit.")
        Double tomcatThreadsMax,

        @Schema(description = "ERROR log events since start.")
        Double logErrorEvents,
        @Schema(description = "WARN log events since start.")
        Double logWarnEvents
) {
}
