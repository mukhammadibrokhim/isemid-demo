package uz.uzinfocom.app.platform.settings.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Data for creating a new route-access policy row.")
public record RouteAccessPolicyCreateRequest(
        @Schema(description = "Ant-style path pattern, e.g. /v1/auth/**.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{route-policy.pattern.required}")
        @Size(max = 200, message = "{route-policy.pattern.size}")
        String pattern,

        @Schema(description = "First-match-wins precedence order.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{route-policy.display-order.required}")
        Integer displayOrder,

        @Schema(description = "Whether this pattern is publicly accessible without authentication.")
        Boolean open,

        @Schema(description = "Whether the X-Organization-Id header is required for this pattern.")
        Boolean organizationHeaderRequired,

        @Schema(description = "Whether role/permission validation is required for this pattern.")
        Boolean roleValidationRequired,

        @Schema(description = "Soft on/off toggle without deleting the row.")
        Boolean enabled,

        @Schema(description = "Free-text note for administrators.")
        @Size(max = 500, message = "{route-policy.description.size}")
        String description
) {
}
