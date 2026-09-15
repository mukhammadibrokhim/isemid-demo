package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a dev-panel position/department lookup entry.")
public record DevPositionCreateRequest(
        @Schema(description = "Position/department name.", example = "Backend Developer")
        @NotBlank(message = "{dev-position.name.required}")
        @Size(max = 150, message = "{dev-position.name.size}")
        String name
) {
}
