package uz.uzinfocom.app.platform.devpanel.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Dev-panel login: password + TOTP -> short-lived signed token.
 *
 * <p>{@code tokenSecret} / {@code totpEncryptionKey} are optional overrides.
 * When blank, both keys are derived (HMAC-SHA256 with a distinct label each)
 * from the already-mandatory {@code app.crypto.webhook-secret-key}, so
 * enabling two-factor needs no new required env var and tokens / stored TOTP
 * secrets survive restarts and are identical across instances.
 *
 * @param tokenTtl          lifetime of an access token issued at login
 * @param twoFactorRequired when true every account must enrol an authenticator
 *                          app; until it does, its token only opens the
 *                          change-password and 2FA-setup endpoints
 * @param issuer            label shown inside the authenticator app
 */
@ConfigurationProperties(prefix = "app.dev.auth")
public record DevAuthProperties(
        String tokenSecret,
        String totpEncryptionKey,
        Duration tokenTtl,
        Boolean twoFactorRequired,
        String issuer
) {
    public DevAuthProperties {
        tokenSecret = tokenSecret == null ? "" : tokenSecret;
        totpEncryptionKey = totpEncryptionKey == null ? "" : totpEncryptionKey;
        tokenTtl = tokenTtl == null ? Duration.ofHours(8) : tokenTtl;
        twoFactorRequired = twoFactorRequired == null || twoFactorRequired;
        issuer = issuer == null || issuer.isBlank() ? "ISEMID Dev Panel" : issuer;
    }
}
