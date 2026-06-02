package uz.uzinfocom.app.platform.security.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.iam.application.sync.IamSyncService;
import uz.uzinfocom.app.platform.iam.application.sync.dto.IamSyncResult;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.security.claims.ExternalIdentityPayload;
import uz.uzinfocom.app.platform.security.claims.IdentityClaimExtractorRegistry;
import uz.uzinfocom.app.platform.security.jwt.JwtProviderResolver;
import uz.uzinfocom.app.platform.security.principal.PrincipalOrganization;
import uz.uzinfocom.app.platform.security.principal.PrincipalUser;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FederatedJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtProviderResolver jwtProviderResolver;
    private final IdentityClaimExtractorRegistry extractorRegistry;
    private final IamSyncService iamSyncService;
    private final SecurityAuthorityService securityAuthorityService;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        String providerKey = jwtProviderResolver.resolveProviderKey(jwt);

        ExternalIdentityPayload payload = extractorRegistry.extract(providerKey, jwt);

        IamSyncResult syncResult = iamSyncService.synchronize(
                payload,
                jwt.getTokenValue()
        );

        User user = syncResult.user();

        PrincipalUser principal = new PrincipalUser(
                user.getId(),
                user.getUuid(),
                user.getUsername(),
                user.getNnuzb(),
                user.getActive(),
                null
        );

        List<PrincipalOrganization> tokenOrganizations = syncResult.organizations()
                .stream()
                .map(this::toPrincipalOrganization)
                .toList();

        Collection<? extends GrantedAuthority> authorities =
                securityAuthorityService.loadAuthoritiesByRoles(syncResult.roles());

        return new FederatedAuthenticationToken(
                jwt,
                principal,
                authorities,
                tokenOrganizations
        );
    }

    private PrincipalOrganization toPrincipalOrganization(Organization organization) {
        return new PrincipalOrganization(
                organization.getId(),
                organization.getUuid(),
                organization.getName()
        );
    }
}