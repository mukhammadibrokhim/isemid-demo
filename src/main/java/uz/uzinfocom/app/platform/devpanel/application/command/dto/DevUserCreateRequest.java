package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.devpanel.domain.DevUserRole;

@Schema(description = "Request to provision a new developer-monitoring-panel login.")
public record DevUserCreateRequest(
        @Schema(description = "Username for the new dev-panel account.", example = "dev-oncall")
        @NotBlank(message = "{dev-user.username.required}")
        @Size(max = 100, message = "{dev-user.username.size}")
        String username,

        @Schema(description = "Access tier for the new account: SUPER_ADMIN (full CRUD, manages other "
                + "dev-panel accounts), ADMIN (CRUD except delete) or USER (read-only). Defaults to USER "
                + "when omitted.", defaultValue = "USER")
        DevUserRole role,

        @Schema(description = "Contact email for this account.", example = "dev-oncall@example.com")
        @NotBlank(message = "{dev-user.email.required}")
        @Size(max = 150, message = "{dev-user.email.size}")
        @Email(message = "{dev-user.email.invalid}")
        String email,

        @Schema(description = "Full name, for display in the account list. Optional.")
        @Size(max = 200, message = "{dev-user.full-name.size}")
        String fullName,

        @Schema(description = "Contact phone number, for display in the account list. Optional.")
        @Size(max = 32, message = "{dev-user.phone.size}")
        String phone,

        @Schema(description = "Id of a position/department from GET /v1/dev/ref/positions. Optional.")
        @Positive(message = "{dev-user.position-id.positive}")
        Long positionId
) {
}
