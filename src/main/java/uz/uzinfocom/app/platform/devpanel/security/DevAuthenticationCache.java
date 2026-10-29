package uz.uzinfocom.app.platform.devpanel.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Caches the outcome of {@link DaoAuthenticationProvider#authenticate} for
 * the dev panel's stateless HTTP Basic chain, keyed by username+password, so
 * a repeat request with the same credentials skips the BCrypt comparison and
 * the {@code DevUser} DB lookup (see SecurityCacheNames.DEV_PANEL_AUTHENTICATION).
 * Only successful authentications are cached - a thrown
 * {@code AuthenticationException} is never cached by {@code @Cacheable}, so
 * failed attempts always hit the real check. Depends on the concrete
 * {@code DaoAuthenticationProvider} type (rather than the
 * {@code AuthenticationProvider} interface) so injection stays unambiguous
 * if another {@code AuthenticationProvider} bean is added elsewhere later.
 * The cache key is a SHA-256 digest of username+password, never the
 * plaintext password, so a heap dump or cache inspection can't reveal it.
 */
@Service
@RequiredArgsConstructor
public class DevAuthenticationCache {

    private final DaoAuthenticationProvider delegate;

    @Cacheable(
            cacheManager = "securityCacheManager",
            cacheNames = SecurityCacheNames.DEV_PANEL_AUTHENTICATION,
            key = "T(uz.uzinfocom.app.platform.devpanel.security.DevAuthenticationCache).cacheKey(#username, #password)"
    )
    public Authentication authenticate(String username, String password) {
        return delegate.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    public static String cacheKey(String username, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((username + ':' + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 is not available in this JVM", unavailable);
        }
    }
}
