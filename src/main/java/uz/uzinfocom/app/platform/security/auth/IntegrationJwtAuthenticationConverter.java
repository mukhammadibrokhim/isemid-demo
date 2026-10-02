package uz.uzinfocom.app.platform.security.auth;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.security.jwt.integration.IntegrationTokenIssuer;
import uz.uzinfocom.app.platform.security.principal.IntegrationClientPrincipal;

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

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String clientId = jwt.getSubject();
        String sourceKey = jwt.getClaimAsString(IntegrationTokenIssuer.SOURCE_KEY_CLAIM);
        Long organizationId = Long.valueOf(jwt.getClaimAsString(IntegrationTokenIssuer.ORGANIZATION_ID_CLAIM));
        UUID organizationUuid = UUID.fromString(jwt.getClaimAsString(IntegrationTokenIssuer.ORGANIZATION_UUID_CLAIM));

        IntegrationClientPrincipal principal =
                new IntegrationClientPrincipal(clientId, sourceKey, organizationId, organizationUuid);

        String scopeClaim = jwt.getClaimAsString(IntegrationTokenIssuer.SCOPE_CLAIM);
        return new IntegrationClientAuthenticationToken(jwt, principal, IntegrationScopeAuthorities.from(scopeClaim));
    }
}
