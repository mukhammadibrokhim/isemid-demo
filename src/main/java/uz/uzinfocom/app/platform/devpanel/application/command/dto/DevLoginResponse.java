package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Result of a login step. Either a full session (token set) or, when the account has "
        + "an authenticator app and no code was sent yet, an OTP challenge (otpRequired + challengeToken).")
public record DevLoginResponse(
        @Schema(description = "Signed access token, sent as 'Authorization: Bearer <token>'. Null while "
                + "otpRequired is true.")
        String token,
        String tokenType,
        @Schema(description = "Expiry of the access token, or of the challengeToken while otpRequired.")
        Instant expiresAt,

        @Schema(description = "True when this token is 'limited': the account still has to enrol an "
                + "authenticator app. It only opens GET .../me, PATCH .../me/password and "
                + "POST .../me/2fa/setup + /confirm; everything else answers 403 DEV_USER_TWO_FACTOR_REQUIRED.")
        boolean twoFactorEnrollmentRequired,

        @Schema(description = "True when the account must also change its password before anything else.")
        boolean mustChangePassword,

        @Schema(description = "True = the password was right and the account has 2FA: show the code screen "
                + "and call POST .../auth/verify-otp with challengeToken + the 6-digit code. No access yet.")
        boolean otpRequired,

        @Schema(description = "Opaque proof of the password step, valid 5 minutes, useless as a Bearer token. "
                + "Only set while otpRequired is true.")
        String challengeToken
) {
    private static final String BEARER = "Bearer";

    public static DevLoginResponse session(
            String token, Instant expiresAt, boolean twoFactorEnrollmentRequired, boolean mustChangePassword
    ) {
        return new DevLoginResponse(token, BEARER, expiresAt, twoFactorEnrollmentRequired, mustChangePassword, false, null);
    }

    public static DevLoginResponse otpChallenge(String challengeToken, Instant expiresAt, boolean mustChangePassword) {
        return new DevLoginResponse(null, null, expiresAt, false, mustChangePassword, true, challengeToken);
    }
}
