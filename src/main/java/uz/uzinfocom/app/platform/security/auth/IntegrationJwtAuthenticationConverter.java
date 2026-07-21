package uz.uzinfocom.app.platform.security.auth;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.security.jwt.integration.IntegrationTokenIssuer;
import uz.uzinfocom.app.platform.security.principal.IntegrationClientPrincipal;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Converts a verified integration JWT into an
 * {@link IntegrationClientAuthenticationToken}. Deliberately does NOT call
 * {@code IamSyncService}/{@code IdentityClaimExtractorRegistry}/
 * {@code SecurityUserCacheService} the way {@link FederatedJwtAuthenticationConverter}
 * does for human tokens — there is no user to sync here, and doing so would
 * be both wrong (these claims aren't human-identity claims) and a needless
 * DB/cache round trip on every request. Everything is derived from the
 * token's own baked-in claims.
 */
@Component
public class IntegrationJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String SCOPE_AUTHORITY_PREFIX = "SCOPE_";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String clientId = jwt.getSubject();
        String sourceKey = jwt.getClaimAsString(IntegrationTokenIssuer.SOURCE_KEY_CLAIM);
        Long organizationId = Long.valueOf(jwt.getClaimAsString(IntegrationTokenIssuer.ORGANIZATION_ID_CLAIM));
        UUID organizationUuid = UUID.fromString(jwt.getClaimAsString(IntegrationTokenIssuer.ORGANIZATION_UUID_CLAIM));

        IntegrationClientPrincipal principal =
                new IntegrationClientPrincipal(clientId, sourceKey, organizationId, organizationUuid);

        return new IntegrationClientAuthenticationToken(jwt, principal, scopeAuthorities(jwt));
    }

    private Set<GrantedAuthority> scopeAuthorities(Jwt jwt) {
        String scopeClaim = jwt.getClaimAsString(IntegrationTokenIssuer.SCOPE_CLAIM);

        if (!StringUtils.hasText(scopeClaim)) {
            return Set.of();
        }

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        for (String scope : scopeClaim.split(" ")) {
            if (StringUtils.hasText(scope)) {
                authorities.add(new SimpleGrantedAuthority(SCOPE_AUTHORITY_PREFIX + scope));
            }
        }

        return authorities;
    }
}
