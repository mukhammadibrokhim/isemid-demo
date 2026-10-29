package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to change the calling dev-panel account's own password.")
public record DevUserChangePasswordRequest(
        @Schema(description = "The account's current password.")
        @NotBlank(message = "{dev-user.password.current.required}")
        String currentPassword,

        @Schema(description = "The new password to set.")
        @NotBlank(message = "{dev-user.password.new.required}")
        @Size(min = 8, max = 100, message = "{dev-user.password.new.size}")
        String newPassword
) {
}
