package uz.uzinfocom.app.platform.devpanel.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * RFC 6238 TOTP (HMAC-SHA1, 6 digits, 30s step) - the exact profile Google
 * Authenticator, Authy and Microsoft Authenticator implement. Self-contained
 * so enabling two-factor adds no third-party dependency.
 */
public final class Totp {

    public static final long STEP_SECONDS = 30;
    private static final int DIGITS = 6;
    private static final int SECRET_BYTES = 20;
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private Totp() {
    }

    public static String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return base32Encode(bytes);
    }

    public static long currentStep(long epochSeconds) {
        return epochSeconds / STEP_SECONDS;
    }

    /**
     * @return the time step the code matched (current step +/- {@code window}),
     * or {@code -1} if it matched none. The caller must still reject a step
     * that is not greater than the account's last used one.
     */
    public static long matchStep(String base32Secret, String code, long nowEpochSeconds, int window) {
        if (code == null || code.length() != DIGITS || !code.chars().allMatch(Character::isDigit)) {
            return -1;
        }
        byte[] key = base32Decode(base32Secret);
        long current = currentStep(nowEpochSeconds);
        long matched = -1;
        // No early exit: constant work whatever the input.
        for (long step = current - window; step <= current + window; step++) {
            if (constantTimeEquals(codeForStep(key, step), code)) {
                matched = step;
            }
        }
        return matched;
    }

    public static String codeForStep(byte[] key, long step) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(step).array());
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % (int) Math.pow(10, DIGITS);
            return String.format("%0" + DIGITS + "d", otp);
        } catch (GeneralSecurityException failure) {
            throw new IllegalStateException("HmacSHA1 is not available in this JVM", failure);
        }
    }

    /** {@code otpauth://} URI the authenticator app scans as a QR code (the frontend renders the QR). */
    public static String otpauthUri(String issuer, String account, String base32Secret) {
        String label = encode(issuer) + ":" + encode(account);
        return "otpauth://totp/" + label
                + "?secret=" + base32Secret
                + "&issuer=" + encode(issuer)
                + "&algorithm=SHA1&digits=" + DIGITS + "&period=" + STEP_SECONDS;
    }

    static String base32Encode(byte[] data) {
        StringBuilder out = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bits = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                out.append(BASE32_ALPHABET.charAt((buffer >> (bits - 5)) & 0x1F));
                bits -= 5;
            }
        }
        if (bits > 0) {
            out.append(BASE32_ALPHABET.charAt((buffer << (5 - bits)) & 0x1F));
        }
        return out.toString();
    }

    static byte[] base32Decode(String encoded) {
        String clean = encoded.replace("=", "").replace(" ", "").toUpperCase();
        byte[] out = new byte[clean.length() * 5 / 8];
        int buffer = 0;
        int bits = 0;
        int index = 0;
        for (char c : clean.toCharArray()) {
            int value = BASE32_ALPHABET.indexOf(c);
            if (value < 0) {
                throw new IllegalArgumentException("Invalid base32 character");
            }
            buffer = (buffer << 5) | value;
            bits += 5;
            if (bits >= 8) {
                out[index++] = (byte) ((buffer >> (bits - 8)) & 0xFF);
                bits -= 8;
            }
        }
        return out;
    }

    static boolean constantTimeEquals(String a, String b) {
        return java.security.MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
