package uz.uzinfocom.app.platform.devpanel.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 * Thin {@link AuthenticationProvider} facade over {@link DevAuthenticationCache}
 * so the dev panel's {@code SecurityFilterChain} (see DevPanelSecurityConfig)
 * can plug in caching transparently, without HttpSecurity needing to know
 * about the cache itself. Also applies {@link DevLoginAttemptLimiter}: a
 * blocked username/IP is refused with the same generic
 * {@link BadCredentialsException} as a wrong password, so the response
 * never tells an attacker whether a guess was right or the key is locked.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CachingDevAuthenticationProvider implements AuthenticationProvider {

    private final DevAuthenticationCache authenticationCache;
    private final DevLoginAttemptLimiter loginAttemptLimiter;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());
        String ip = remoteAddress(authentication);

        if (loginAttemptLimiter.isBlocked(username, ip)) {
            log.warn("Dev-panel login refused: too many failed attempts. username={}, ip={}", username, ip);
            throw new BadCredentialsException("Bad credentials");
        }

        try {
            Authentication result = authenticationCache.authenticate(username, password);
            loginAttemptLimiter.recordSuccess(username);
            return result;
        } catch (AuthenticationException failure) {
            loginAttemptLimiter.recordFailure(username, ip);
            throw failure;
        }
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static String remoteAddress(Authentication authentication) {
        return authentication.getDetails() instanceof WebAuthenticationDetails details
                ? details.getRemoteAddress()
                : null;
    }
}
