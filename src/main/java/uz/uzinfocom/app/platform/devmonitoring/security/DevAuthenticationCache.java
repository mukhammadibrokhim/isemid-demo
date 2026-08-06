package uz.uzinfocom.app.platform.devmonitoring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;

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
 */
@Service
@RequiredArgsConstructor
public class DevAuthenticationCache {

    private final DaoAuthenticationProvider delegate;

    @Cacheable(
            cacheManager = "securityCacheManager",
            cacheNames = SecurityCacheNames.DEV_PANEL_AUTHENTICATION,
            key = "#username + ':' + #password"
    )
    public Authentication authenticate(String username, String password) {
        return delegate.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}
