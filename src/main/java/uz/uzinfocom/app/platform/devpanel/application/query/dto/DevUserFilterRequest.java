package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.platform.devpanel.domain.DevUserRole;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Filter and pagination parameters for dev-panel accounts.")
public record DevUserFilterRequest(
        @Min(value = 1, message = "{pagination.page.min}")
        Integer page,

        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 200, message = "{pagination.size.max}")
        Integer size,

        @Schema(allowableValues = {"id", "username", "createdAt", "updatedAt"})
        String sortBy,

        @Schema(allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Filter by username (partial, case-insensitive).")
        String username,

        @Schema(description = "Filter by whether the account is enabled.")
        Boolean enabled,

        @Schema(description = "Filter by access tier.")
        DevUserRole role,

        @Schema(description = "Filter by assigned position/department id.")
        Long positionId
) implements PageableRequest {
}
