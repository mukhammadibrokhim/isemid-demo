package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import java.time.Instant;

/**
 * Summary row for the table view - {@code GET /v1/dev/requests}. Full detail
 * (query string, content types, exception/root-cause, message, user agent)
 * is only fetched per-row via {@code GET /v1/dev/requests/{id}}, see
 * {@link DevRequestLogDetailResponse}.
 */
public record DevRequestLogResponse(
        Long id,
        String traceId,
        String method,
        String path,
        Integer httpStatus,
        String outcome,
        Long durationMs,
        String principal,
        String organizationId,
        String clientIp,
        Instant occurredAt
) {
}
