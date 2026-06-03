package uz.uzinfocom.app.platform.security.auth;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uz.uzinfocom.app.platform.security.principal.PrincipalOrganization;
import uz.uzinfocom.app.platform.security.principal.PrincipalUser;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public final class FederatedAuthenticationToken extends AbstractAuthenticationToken {

    private final Jwt jwt;
    private final PrincipalUser principal;
    private final List<PrincipalOrganization> tokenOrganizations;

    public FederatedAuthenticationToken(
            Jwt jwt,
            PrincipalUser principal,
            Collection<? extends GrantedAuthority> authorities,
            List<PrincipalOrganization> tokenOrganizations
    ) {
        super(authorities == null ? List.of() : authorities);
        this.jwt = jwt;
        this.principal = principal;
        this.tokenOrganizations = tokenOrganizations == null ? List.of() : List.copyOf(tokenOrganizations);
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt.getTokenValue();
    }

    @Override
    public PrincipalUser getPrincipal() {
        return principal;
    }

    public Long getUserId() {
        return principal.id();
    }

    public UUID getUserUuid() {
        return principal.uuid();
    }

    public UUID getSelectedOrganizationUuid() {
        return principal.selectedOrganizationUuid();
    }
}
