package uz.uzinfocom.app.platform.crypto;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES/GCM encrypt/decrypt for the one webhook credential that must be
 * recoverable as plaintext to be sent back out on an outbound call - see
 * {@code IntegrationClientCommandService#updateWebhook} (encrypts on write)
 * and {@code OutboundWebhookClient} (decrypts immediately before the HTTP
 * call). The encrypted value is never logged and never returned from any
 * response DTO in either form.
 *
 * <p>Output is base64({@code iv || ciphertext+tag}) - a fresh random IV per
 * call, prepended so decryption never needs a second stored value.
 */
@Component
public class WebhookSecretCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int KEY_LENGTH_BYTES = 32;

    private final SecretKeySpec key;
    private final SecureRandom secureRandom = new SecureRandom();

    public WebhookSecretCipher(WebhookCryptoProperties properties) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(properties.secretKey());
        } catch (IllegalArgumentException notBase64) {
            throw new IllegalStateException(
                    "app.crypto.webhook-secret-key must be base64-encoded", notBase64);
        }
        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "app.crypto.webhook-secret-key must decode to a 256-bit (32-byte) AES key, got "
                            + keyBytes.length + " bytes");
        }
        this.key = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException failure) {
            throw new IllegalStateException("Failed to encrypt webhook secret", failure);
        }
    }

    public String decrypt(String encoded) {
        if (encoded == null) {
            return null;
        }

        byte[] combined = Base64.getDecoder().decode(encoded);
        if (combined.length <= IV_LENGTH_BYTES) {
            throw new IllegalStateException("Encrypted webhook secret is malformed");
        }
        byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH_BYTES);
        byte[] ciphertext = Arrays.copyOfRange(combined, IV_LENGTH_BYTES, combined.length);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException failure) {
            throw new IllegalStateException("Failed to decrypt webhook secret", failure);
        }
    }
}
