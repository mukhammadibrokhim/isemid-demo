package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import java.time.Instant;
import java.util.UUID;

public record DevLoginHistoryDetailResponse(
        Long id,
        String provider,
        String username,
        UUID userId,
        Boolean success,
        String failureReason,
        String ip,
        String userAgent,
        String traceId,
        Instant occurredAt
) {
}
