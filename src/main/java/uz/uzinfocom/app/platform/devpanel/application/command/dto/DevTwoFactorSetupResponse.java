package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Start of authenticator-app enrolment. Render otpauthUri as a QR code (or let the "
        + "user type the secret) and have them confirm with the first code.")
public record DevTwoFactorSetupResponse(
        @Schema(description = "Base32 secret, for manual entry. Shown only here.")
        String secret,

        @Schema(description = "otpauth:// URI to encode as a QR code for Google Authenticator / Authy / "
                + "Microsoft Authenticator.")
        String otpauthUri
) {
}
