package uz.uzinfocom.app.platform.security.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.security.filter.IntegrationCredentialAuthenticator;
import uz.uzinfocom.app.platform.security.jwt.JwtProviderResolver;

/**
 * Converts a verified SSO/DHP M2M token into an
 * {@link IntegrationClientAuthenticationToken} for the matching
 * {@code EXTERNAL_TOKEN} client. Like {@link IntegrationJwtAuthenticationConverter}
 * it never touches IAM sync — there is no user behind an M2M token.
 */
@Component
@RequiredArgsConstructor
public class ExternalTokenJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtProviderResolver jwtProviderResolver;
    private final ExternalTokenClientResolver externalTokenClientResolver;
    private final IntegrationCredentialAuthenticator credentialAuthenticator;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        String providerKey = jwtProviderResolver.resolveProviderKey(jwt);

        IntegrationClient client = externalTokenClientResolver.resolve(providerKey, jwt.getClaims())
                .orElseThrow(() -> new BadCredentialsException("No integration client registered for this token"));

        return new IntegrationClientAuthenticationToken(
                jwt,
                credentialAuthenticator.principalOf(client),
                IntegrationScopeAuthorities.from(client.getScopes())
        );
    }
}
