package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import java.time.Instant;

public record DevRequestLogDetailResponse(
        Long id,
        String traceId,
        String method,
        String route,
        String path,
        String query,
        Integer httpStatus,
        String outcome,
        Long durationMs,
        String clientIp,
        String principal,
        String organizationId,
        String requestContentType,
        String responseContentType,
        Long requestContentLength,
        String errorCode,
        String exceptionType,
        String rootCauseType,
        String message,
        String userAgent,
        Instant occurredAt
) {
}
