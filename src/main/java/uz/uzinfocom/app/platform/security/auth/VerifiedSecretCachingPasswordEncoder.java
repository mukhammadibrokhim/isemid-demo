package uz.uzinfocom.app.platform.security.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

/**
 * Remembers successful {@link #matches} results so an integration client that
 * sends its API key / Basic secret on every request pays the BCrypt cost
 * (~50-100 ms of CPU) once per {@code ttl}, not once per request - without it a
 * single busy submitter could saturate the CPU on hashing alone.
 *
 * <p>Only successes are cached, keyed by SHA-256 of (stored hash, raw secret):
 * neither the secret nor anything reversible to it is held in memory, a wrong
 * secret is always BCrypt-checked, and rotating a credential changes the stored
 * hash and with it the key, so the old secret stops matching at once.
 * Revocation/deactivation is unaffected - callers still re-read the client row
 * and check {@code active} on every request.
 */
public class VerifiedSecretCachingPasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder delegate;
    private final Cache<String, Boolean> verified;

    public VerifiedSecretCachingPasswordEncoder(PasswordEncoder delegate, Duration ttl, long maxEntries) {
        this.delegate = delegate;
        this.verified = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxEntries)
                .build();
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return delegate.matches(rawPassword, encodedPassword);
        }

        String key = cacheKey(rawPassword, encodedPassword);
        if (verified.getIfPresent(key) != null) {
            return true;
        }

        boolean matches = delegate.matches(rawPassword, encodedPassword);
        if (matches) {
            verified.put(key, Boolean.TRUE);
        }
        return matches;
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return delegate.upgradeEncoding(encodedPassword);
    }

    private static String cacheKey(CharSequence rawPassword, String encodedPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(encodedPassword.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is not available", impossible);
        }
    }
}
