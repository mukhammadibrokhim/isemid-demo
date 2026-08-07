package uz.uzinfocom.app.platform.devmonitoring.application.query.dto;

import uz.uzinfocom.app.platform.devmonitoring.domain.DevErrorStatus;

import java.time.Instant;

public record DevErrorDetailResponse(
        Long id,
        String traceId,
        String errorCode,
        Integer httpStatus,
        String exceptionType,
        String path,
        String method,
        String principal,
        String message,
        Instant occurredAt,
        DevErrorStatus status,
        Long resolvedBy,
        Instant resolvedAt
) {
}
