package uz.uzinfocom.app.platform.devpanel.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * Key material for dev-panel tokens and stored TOTP secrets. An explicit
 * {@code app.dev.auth.*} value wins; otherwise the key is derived from
 * {@code app.crypto.webhook-secret-key} (required at startup anyway) with a
 * per-purpose label, so the token key and the TOTP-encryption key are
 * independent of each other. Read through {@code @Value} on purpose:
 * {@code platform} must not depend on {@code orchestration}.
 */
@Component
public class DevAuthKeys {

    private static final String TOKEN_LABEL = "dev-panel-token-v1";
    private static final String TOTP_LABEL = "dev-panel-totp-v1";

    private final byte[] tokenKey;
    private final byte[] totpKey;

    public DevAuthKeys(
            DevAuthProperties properties,
            @Value("${app.crypto.webhook-secret-key:}") String baseSecret
    ) {
        this.tokenKey = resolve(properties.tokenSecret(), baseSecret, TOKEN_LABEL);
        this.totpKey = resolve(properties.totpEncryptionKey(), baseSecret, TOTP_LABEL);
    }

    public byte[] tokenKey() {
        return tokenKey.clone();
    }

    public byte[] totpKey() {
        return totpKey.clone();
    }

    private static byte[] resolve(String explicit, String baseSecret, String label) {
        String material = explicit != null && !explicit.isBlank() ? explicit : baseSecret;
        if (material == null || material.isBlank()) {
            throw new IllegalStateException(
                    "Dev-panel auth needs key material: set app.dev.auth.token-secret / "
                            + "app.dev.auth.totp-encryption-key or app.crypto.webhook-secret-key");
        }
        return hmac(material.getBytes(StandardCharsets.UTF_8), label);
    }

    private static byte[] hmac(byte[] key, String label) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(label.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException failure) {
            throw new IllegalStateException("HmacSHA256 is not available in this JVM", failure);
        }
    }
}
