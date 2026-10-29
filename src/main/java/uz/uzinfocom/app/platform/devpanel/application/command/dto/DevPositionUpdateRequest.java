package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a dev-panel position/department lookup entry.")
public record DevPositionUpdateRequest(
        @Schema(description = "Position/department name.", example = "Backend Developer")
        @NotBlank(message = "{dev-position.name.required}")
        @Size(max = 150, message = "{dev-position.name.size}")
        String name,

        @Schema(description = "Whether the position is selectable for a dev-user.")
        @NotNull(message = "{dev-position.enabled.required}")
        Boolean enabled
) {
}
