package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Summary row for the table view - {@code GET /v1/dev/logins}. Full detail
 * (failure reason, user agent, trace id) is only fetched per-row via
 * {@code GET /v1/dev/logins/{id}}, see {@link DevLoginHistoryDetailResponse}.
 */
public record DevLoginHistoryResponse(
        Long id,
        String provider,
        String username,
        UUID userId,
        Boolean success,
        String ip,
        Instant occurredAt
) {
}
