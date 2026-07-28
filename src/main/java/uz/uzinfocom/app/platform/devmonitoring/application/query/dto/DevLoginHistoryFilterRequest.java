package uz.uzinfocom.app.platform.devmonitoring.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

import java.time.Instant;

@Schema(description = "Filter and pagination parameters for the login-attempt history.")
public record DevLoginHistoryFilterRequest(
        @Min(value = 1, message = "{pagination.page.min}")
        Integer page,

        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 200, message = "{pagination.size.max}")
        Integer size,

        @Schema(allowableValues = {"id", "occurredAt", "username", "provider"})
        String sortBy,

        @Schema(allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Filter by the resolved username/subject.")
        String username,

        @Schema(description = "Filter by login-proxy provider key (e.g. sso-web, dhp-web).")
        String provider,

        @Schema(description = "Filter by whether the attempt succeeded.")
        Boolean success,

        @Schema(description = "Only include attempts at or after this instant.")
        Instant from,

        @Schema(description = "Only include attempts at or before this instant.")
        Instant to
) implements PageableRequest {
}
