package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Two-factor is now on. Enabling it changes the account's credential version, so the "
        + "old (limited) token is dead - use the fresh 'session' token returned here.")
public record DevTwoFactorConfirmResponse(
        DevLoginResponse session,

        @Schema(description = "One-time recovery codes, usable instead of an authenticator code if the phone "
                + "is lost. Shown ONCE - only their hashes are stored.")
        List<String> recoveryCodes
) {
}
