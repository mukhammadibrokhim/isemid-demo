package uz.uzinfocom.app.orchestration.webhook.crypto;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebhookSecretCipherTest {

    private final WebhookSecretCipher cipher = new WebhookSecretCipher(new WebhookCryptoProperties(randomBase64Key()));

    @Test
    void encryptThenDecryptReturnsTheOriginalPlaintext() {
        String encrypted = cipher.encrypt("super-secret-token");

        assertThat(encrypted).isNotBlank();
        assertThat(encrypted).isNotEqualTo("super-secret-token");
        assertThat(cipher.decrypt(encrypted)).isEqualTo("super-secret-token");
    }

    @Test
    void encryptingTheSameValueTwiceProducesDifferentCiphertext() {
        String first = cipher.encrypt("same-value");
        String second = cipher.encrypt("same-value");

        assertThat(first).isNotEqualTo(second);
        assertThat(cipher.decrypt(first)).isEqualTo("same-value");
        assertThat(cipher.decrypt(second)).isEqualTo("same-value");
    }

    @Test
    void encryptAndDecryptReturnNullForNull() {
        assertThat(cipher.encrypt(null)).isNull();
        assertThat(cipher.decrypt(null)).isNull();
    }

    @Test
    void constructorRejectsAKeyThatIsNotBase64() {
        assertThatThrownBy(() -> new WebhookSecretCipher(new WebhookCryptoProperties("not-base64-!!!")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void constructorRejectsAKeyOfTheWrongLength() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);

        assertThatThrownBy(() -> new WebhookSecretCipher(new WebhookCryptoProperties(shortKey)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("256-bit");
    }

    @Test
    void decryptRejectsATruncatedValue() {
        String tooShort = Base64.getEncoder().encodeToString(new byte[4]);

        assertThatThrownBy(() -> cipher.decrypt(tooShort))
                .isInstanceOf(IllegalStateException.class);
    }

    private static String randomBase64Key() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
