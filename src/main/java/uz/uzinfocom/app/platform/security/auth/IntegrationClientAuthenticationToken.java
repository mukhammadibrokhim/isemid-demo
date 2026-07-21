package uz.uzinfocom.app.platform.security.auth;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uz.uzinfocom.app.platform.security.principal.IntegrationClientPrincipal;

import java.util.Collection;
import java.util.List;

/**
 * The {@link org.springframework.security.core.Authentication} for an
 * inbound-integration (machine) caller — structural twin of
 * {@link FederatedAuthenticationToken}, but carrying an
 * {@link IntegrationClientPrincipal} instead of a human {@code PrincipalUser}.
 */
@Getter
public final class IntegrationClientAuthenticationToken extends AbstractAuthenticationToken {

    private final Jwt jwt;
    private final IntegrationClientPrincipal principal;

    public IntegrationClientAuthenticationToken(
            Jwt jwt,
            IntegrationClientPrincipal principal,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities == null ? List.of() : authorities);
        this.jwt = jwt;
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt.getTokenValue();
    }

    @Override
    public IntegrationClientPrincipal getPrincipal() {
        return principal;
    }
}
