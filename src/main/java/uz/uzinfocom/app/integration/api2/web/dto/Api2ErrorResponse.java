package uz.uzinfocom.app.integration.api2.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

public record Api2ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String operation,
        Integer upstreamStatus,
        String upstreamCode,
        String upstreamMessage,
        String upstreamDetail,
        String traceId,
        List<FieldValidationError> fieldErrors,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long retryAfterSeconds
) {
}
