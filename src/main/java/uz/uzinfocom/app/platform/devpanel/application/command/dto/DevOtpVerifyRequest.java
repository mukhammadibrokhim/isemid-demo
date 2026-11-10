package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Second login step: the challengeToken from POST .../auth/login plus the 6-digit "
        + "authenticator code (or a one-time recovery code).")
public record DevOtpVerifyRequest(
        @NotBlank(message = "{dev-user.login.challenge.required}")
        @Size(max = 500)
        String challengeToken,

        @NotBlank(message = "{dev-user.login.otp.required}")
        @Size(max = 32)
        String otp
) {
}
