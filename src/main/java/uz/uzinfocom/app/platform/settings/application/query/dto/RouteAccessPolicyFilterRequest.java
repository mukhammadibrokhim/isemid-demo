package uz.uzinfocom.app.platform.settings.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Filter and pagination parameters for the route-access policy list.")
public record RouteAccessPolicyFilterRequest(
        @Min(value = 1, message = "{pagination.page.min}")
        Integer page,

        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 200, message = "{pagination.size.max}")
        Integer size,

        @Schema(allowableValues = {"id", "pattern", "displayOrder"})
        String sortBy,

        @Schema(allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Text search over the pattern.")
        String search,

        @Schema(description = "Filter by enabled/disabled.")
        Boolean enabled
) implements PageableRequest {
}
