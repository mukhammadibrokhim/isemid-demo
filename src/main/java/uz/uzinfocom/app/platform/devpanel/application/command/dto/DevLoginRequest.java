package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dev-panel login: username + password, plus the 6-digit authenticator code "
        + "(or a one-time recovery code) once the account has two-factor enabled.")
public record DevLoginRequest(
        @NotBlank(message = "{dev-user.login.username.required}")
        @Size(max = 100)
        String username,

        @NotBlank(message = "{dev-user.login.password.required}")
        @Size(max = 200)
        String password,

        @Schema(description = "6-digit TOTP code, or a recovery code. Ignored (may be omitted) for an "
                + "account that has not enrolled an authenticator yet.")
        @Size(max = 32)
        String otp
) {
}
