package uz.uzinfocom.app.integration.api2.api.dto;

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
        List<FieldValidationError> fieldErrors
) {
}
