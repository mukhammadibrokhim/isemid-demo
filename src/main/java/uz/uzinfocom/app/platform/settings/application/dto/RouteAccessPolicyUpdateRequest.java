package uz.uzinfocom.app.platform.settings.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Data for updating an existing route-access policy row.")
public record RouteAccessPolicyUpdateRequest(
        @NotBlank(message = "{route-policy.pattern.required}")
        @Size(max = 200, message = "{route-policy.pattern.size}")
        String pattern,

        @NotNull(message = "{route-policy.display-order.required}")
        Integer displayOrder,

        Boolean open,
        Boolean organizationHeaderRequired,
        Boolean roleValidationRequired,
        Boolean enabled,

        @Size(max = 500, message = "{route-policy.description.size}")
        String description
) {
}
