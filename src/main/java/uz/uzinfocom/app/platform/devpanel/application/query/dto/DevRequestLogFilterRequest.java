package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

import java.time.Instant;

@Schema(description = "Filter and pagination parameters for the full request (resource-usage) log.")
public record DevRequestLogFilterRequest(
        @Min(value = 1, message = "{pagination.page.min}")
        Integer page,

        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 200, message = "{pagination.size.max}")
        Integer size,

        @Schema(allowableValues = {"id", "occurredAt", "httpStatus", "durationMs", "principal"})
        String sortBy,

        @Schema(allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Filter by the authenticated principal/username who made the request.")
        String principal,

        @Schema(description = "Filter by HTTP method (e.g. GET, POST).")
        String method,

        @Schema(description = "Filter by path, substring match (e.g. /v1/form058).")
        String path,

        @Schema(description = "Filter by HTTP status code.")
        Integer status,

        @Schema(allowableValues = {"success", "rejected", "error"}, description = "Filter by outcome.")
        String outcome,

        @Schema(description = "Filter by organization id (X-Organization-Id header).")
        String organizationId,

        @Schema(description = "Filter by trace id (exact match).")
        String traceId,

        @Schema(description = "Only include requests that occurred at or after this instant.")
        Instant from,

        @Schema(description = "Only include requests that occurred at or before this instant.")
        Instant to
) implements PageableRequest {
}
