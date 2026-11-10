package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Filter and pagination parameters for dev-panel positions/departments.")
public record DevPositionFilterRequest(
        @Min(value = 1, message = "{pagination.page.min}")
        Integer page,

        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 200, message = "{pagination.size.max}")
        Integer size,

        @Schema(allowableValues = {"id", "name", "createdAt", "updatedAt"})
        String sortBy,

        @Schema(allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Filter by name (partial, case-insensitive).")
        String name,

        @Schema(description = "Filter by whether the position is enabled.")
        Boolean enabled
) implements PageableRequest {
}
