package uz.uzinfocom.app.platform.devmonitoring.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevErrorStatus;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

import java.time.Instant;

@Schema(description = "Filter and pagination parameters for the failed-request log.")
public record DevErrorFilterRequest(
        @Min(value = 1, message = "{pagination.page.min}")
        Integer page,

        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 200, message = "{pagination.size.max}")
        Integer size,

        @Schema(allowableValues = {"id", "occurredAt", "httpStatus", "status"})
        String sortBy,

        @Schema(allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Filter by resolution status.")
        DevErrorStatus status,

        @Schema(description = "Filter by error code (see ErrorCode enum).")
        String errorCode,

        @Schema(description = "Filter by trace id (exact match).")
        String traceId,

        @Schema(description = "Only include errors that occurred at or after this instant.")
        Instant from,

        @Schema(description = "Only include errors that occurred at or before this instant.")
        Instant to
) implements PageableRequest {
}
