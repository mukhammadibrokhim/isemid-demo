package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Signed access token for /v1/dev/**. Send it as 'Authorization: Bearer <token>'.")
public record DevLoginResponse(
        String token,
        String tokenType,
        Instant expiresAt,

        @Schema(description = "True when this token is 'limited': the account still has to enrol an "
                + "authenticator app. It only opens GET .../me, PATCH .../me/password and "
                + "POST .../me/2fa/setup + /confirm; everything else answers 403 DEV_USER_TWO_FACTOR_REQUIRED.")
        boolean twoFactorEnrollmentRequired,

        @Schema(description = "True when the account must also change its password before anything else.")
        boolean mustChangePassword
) {
}
