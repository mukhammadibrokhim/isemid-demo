package uz.uzinfocom.app.platform.devmonitoring.application.query.dto;

public record HttpEndpointMetricsResponse(
        String method,
        String uri,
        long totalCount,
        long errorCount,
        double meanDurationMs,
        double maxDurationMs
) {
}
