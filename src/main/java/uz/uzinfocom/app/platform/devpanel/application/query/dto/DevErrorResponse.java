package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import uz.uzinfocom.app.platform.devpanel.domain.DevErrorStatus;

import java.time.Instant;

/**
 * Summary row for the table view - {@code GET /v1/dev/errors}. Full detail
 * (trace id, principal, message) is only fetched per-row via
 * {@code GET /v1/dev/errors/{id}}, see {@link DevErrorDetailResponse}.
 */
public record DevErrorResponse(
        Long id,
        String errorCode,
        Integer httpStatus,
        String exceptionType,
        String path,
        String method,
        Instant occurredAt,
        DevErrorStatus status
) {
}
