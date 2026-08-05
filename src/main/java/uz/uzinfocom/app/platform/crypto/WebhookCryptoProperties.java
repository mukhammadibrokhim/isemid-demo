package uz.uzinfocom.app.platform.crypto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Key material for {@link WebhookSecretCipher}, the only consumer of a
 * reversible secret in this codebase - every other stored credential
 * ({@code IntegrationClient.clientSecretHash}/{@code apiKeyHash}/
 * {@code basicAuthSecretHash}) is one-way hashed via {@code PasswordEncoder}.
 *
 * <p>{@code webhookSecretKey} binds to {@code app.crypto.webhook-secret-key}
 * and must be a base64-encoded 256-bit (32-byte) AES key, supplied via the
 * {@code WEBHOOK_SECRET_KEY} env var per deployment - never committed.
 */
@Validated
@ConfigurationProperties(prefix = "app.crypto")
public record WebhookCryptoProperties(

        @NotBlank String webhookSecretKey
) {
    public String secretKey() {
        return webhookSecretKey;
    }
}
