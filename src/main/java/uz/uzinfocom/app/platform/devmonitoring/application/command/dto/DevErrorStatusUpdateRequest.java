package uz.uzinfocom.app.platform.devmonitoring.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevErrorStatus;

@Schema(description = "New resolution status for a failed-request log entry.")
public record DevErrorStatusUpdateRequest(
        @NotNull(message = "{dev-monitoring.error.status.required}")
        DevErrorStatus status
) {
}
