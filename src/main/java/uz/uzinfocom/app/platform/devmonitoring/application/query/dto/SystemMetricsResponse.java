package uz.uzinfocom.app.platform.devmonitoring.application.query.dto;

public record SystemMetricsResponse(
        Double systemCpuUsage,
        Double processCpuUsage,
        Double heapMemoryUsedBytes,
        Double heapMemoryMaxBytes,
        Double diskFreeBytes,
        Double diskTotalBytes
) {
}
