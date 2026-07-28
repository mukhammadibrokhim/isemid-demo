package uz.uzinfocom.app.platform.devmonitoring.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to provision a new developer-monitoring-panel login.")
public record DevUserCreateRequest(
        @Schema(description = "Username for the new dev-panel account.", example = "dev-oncall")
        @NotBlank(message = "{dev-user.username.required}")
        @Size(max = 100, message = "{dev-user.username.size}")
        String username
) {
}
