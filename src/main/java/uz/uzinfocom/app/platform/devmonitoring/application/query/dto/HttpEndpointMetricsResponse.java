package uz.uzinfocom.app.platform.devmonitoring.application.query.dto;

public record HttpEndpointMetricsResponse(
        String method,
        String uri,
        long totalCount,
        long count2xx,
        long count3xx,
        long count4xx,
        long count5xx,
        long errorCount,
        double meanDurationMs,
        double maxDurationMs,
        double p50DurationMs,
        double p95DurationMs,
        double p99DurationMs
) {
}
