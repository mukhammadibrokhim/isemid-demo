package uz.uzinfocom.app.platform.security.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Builds {@code SCOPE_*} {@link GrantedAuthority}s from an {@code
 * IntegrationClient}'s stored scopes, shared by every place that
 * authenticates an integration-client credential: the client-credentials
 * JWT converter and the API-key/Basic-Auth/IP-allow-list filters. The JWT's
 * {@code scope} claim is space-delimited (OAuth2 convention); {@code
 * IntegrationClient.scopes} is comma-delimited (see {@code
 * IntegrationClientCommandService}) — both are accepted here.
 */
public final class IntegrationScopeAuthorities {

    private static final String SCOPE_AUTHORITY_PREFIX = "SCOPE_";
    private static final Pattern DELIMITER = Pattern.compile("[ ,]+");

    private IntegrationScopeAuthorities() {
    }

    public static Set<GrantedAuthority> from(String scopes) {
        if (!StringUtils.hasText(scopes)) {
            return Set.of();
        }

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        for (String scope : DELIMITER.split(scopes.trim())) {
            if (StringUtils.hasText(scope)) {
                authorities.add(new SimpleGrantedAuthority(SCOPE_AUTHORITY_PREFIX + scope));
            }
        }

        return authorities;
    }
}
