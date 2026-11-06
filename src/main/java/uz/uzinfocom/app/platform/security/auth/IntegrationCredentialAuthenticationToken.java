package uz.uzinfocom.app.platform.security.auth;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.security.principal.IntegrationClientPrincipal;

import java.util.Collection;
import java.util.List;

/**
 * The {@link org.springframework.security.core.Authentication} for an
 * inbound-integration caller authenticated by a non-JWT credential (API key,
 * Basic Auth, or IP allow-list) — structural twin of
 * {@link IntegrationClientAuthenticationToken}, but without a {@link
 * org.springframework.security.oauth2.jwt.Jwt} backing it, since none of
 * these credential kinds involve one.
 */
@Getter
public final class IntegrationCredentialAuthenticationToken extends AbstractAuthenticationToken
        implements IntegrationClientAuthentication {

    private final IntegrationAuthType authType;
    private final IntegrationClientPrincipal principal;

    public IntegrationCredentialAuthenticationToken(
            IntegrationAuthType authType,
            IntegrationClientPrincipal principal,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities == null ? List.of() : authorities);
        this.authType = authType;
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return authType.name();
    }

    @Override
    public IntegrationClientPrincipal getPrincipal() {
        return principal;
    }
}
