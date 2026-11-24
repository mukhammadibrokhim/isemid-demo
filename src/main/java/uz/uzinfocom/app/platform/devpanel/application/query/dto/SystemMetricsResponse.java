package uz.uzinfocom.app.platform.devpanel.application.query.dto;

public record SystemMetricsResponse(
        Double systemCpuUsage,
        Double processCpuUsage,
        Double heapMemoryUsedBytes,
        Double heapMemoryMaxBytes,
        Double diskFreeBytes,
        Double diskTotalBytes
) {
}
