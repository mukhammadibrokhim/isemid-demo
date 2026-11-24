package uz.uzinfocom.app.platform.devpanel.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class TotpTest {

    /** RFC 6238 appendix B SHA-1 key: ASCII "12345678901234567890". */
    private static final byte[] RFC_KEY = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
    private static final String RFC_KEY_BASE32 = Totp.base32Encode(RFC_KEY);

    @Test
    void matchesRfc6238TestVectors() {
        // RFC lists 8-digit codes 94287082 / 07081804 / 14050471; the 6-digit code is their last 6 digits.
        assertThat(Totp.codeForStep(RFC_KEY, 59 / 30)).isEqualTo("287082");
        assertThat(Totp.codeForStep(RFC_KEY, 1111111109L / 30)).isEqualTo("081804");
        assertThat(Totp.codeForStep(RFC_KEY, 1111111111L / 30)).isEqualTo("050471");
    }

    @Test
    void base32RoundTrips() {
        String secret = Totp.generateSecret();

        assertThat(secret).hasSize(32).matches("[A-Z2-7]+");
        assertThat(Totp.base32Encode(Totp.base32Decode(secret))).isEqualTo(secret);
        assertThat(Totp.base32Decode(RFC_KEY_BASE32)).isEqualTo(RFC_KEY);
    }

    @Test
    void acceptsTheCurrentAndAdjacentStepOnly() {
        long now = 1_700_000_000L;
        long step = Totp.currentStep(now);

        assertThat(Totp.matchStep(RFC_KEY_BASE32, Totp.codeForStep(RFC_KEY, step), now, 1)).isEqualTo(step);
        assertThat(Totp.matchStep(RFC_KEY_BASE32, Totp.codeForStep(RFC_KEY, step - 1), now, 1)).isEqualTo(step - 1);
        assertThat(Totp.matchStep(RFC_KEY_BASE32, Totp.codeForStep(RFC_KEY, step + 1), now, 1)).isEqualTo(step + 1);
        assertThat(Totp.matchStep(RFC_KEY_BASE32, Totp.codeForStep(RFC_KEY, step - 2), now, 1)).isEqualTo(-1);
        assertThat(Totp.matchStep(RFC_KEY_BASE32, Totp.codeForStep(RFC_KEY, step + 2), now, 1)).isEqualTo(-1);
    }

    @Test
    void rejectsMalformedCodes() {
        long now = 1_700_000_000L;

        assertThat(Totp.matchStep(RFC_KEY_BASE32, null, now, 1)).isEqualTo(-1);
        assertThat(Totp.matchStep(RFC_KEY_BASE32, "12345", now, 1)).isEqualTo(-1);
        assertThat(Totp.matchStep(RFC_KEY_BASE32, "abcdef", now, 1)).isEqualTo(-1);
        assertThat(Totp.matchStep(RFC_KEY_BASE32, "1234567", now, 1)).isEqualTo(-1);
    }

    @Test
    void buildsAnAuthenticatorUri() {
        String uri = Totp.otpauthUri("ISEMID Dev Panel", "dev admin", "ABCDEFGH");

        assertThat(uri).startsWith("otpauth://totp/ISEMID%20Dev%20Panel:dev%20admin?secret=ABCDEFGH")
                .contains("issuer=ISEMID%20Dev%20Panel")
                .contains("digits=6")
                .contains("period=30");
    }
}
