package uz.uzinfocom.app.platform.devmonitoring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Thin {@link AuthenticationProvider} facade over {@link DevAuthenticationCache}
 * so the dev panel's {@code SecurityFilterChain} (see DevPanelSecurityConfig)
 * can plug in caching transparently, without HttpSecurity needing to know
 * about the cache itself.
 */
@Component
@RequiredArgsConstructor
public class CachingDevAuthenticationProvider implements AuthenticationProvider {

    private final DevAuthenticationCache authenticationCache;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());
        return authenticationCache.authenticate(username, password);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
