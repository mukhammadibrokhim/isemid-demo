package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "First 6-digit code from the authenticator app, proving the enrolment worked.")
public record DevTwoFactorConfirmRequest(
        @NotBlank(message = "{dev-user.two-factor.code.required}")
        @Pattern(regexp = "\\d{6}", message = "{dev-user.two-factor.code.format}")
        String code
) {
}
