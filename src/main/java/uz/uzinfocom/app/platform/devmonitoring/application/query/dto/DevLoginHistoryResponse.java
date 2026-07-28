package uz.uzinfocom.app.platform.devmonitoring.application.query.dto;

import java.time.Instant;

public record DevLoginHistoryResponse(
        Long id,
        String provider,
        String username,
        Boolean success,
        String failureReason,
        String ip,
        String userAgent,
        String traceId,
        Instant occurredAt
) {
}
