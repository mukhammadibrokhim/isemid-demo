package uz.uzinfocom.app.platform.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import uz.uzinfocom.app.platform.security.auth.FederatedJwtAuthenticationConverter;
import uz.uzinfocom.app.platform.security.auth.IntegrationClientAuthenticationToken;
import uz.uzinfocom.app.platform.security.auth.IntegrationJwtAuthenticationConverter;
import uz.uzinfocom.app.platform.security.jwt.integration.IntegrationJwtDecoderConfig;
import uz.uzinfocom.app.platform.security.jwt.integration.IntegrationTokenIssuer;
import uz.uzinfocom.app.platform.security.jwt.integration.IntegrationTokenSigningKey;
import uz.uzinfocom.app.platform.security.jwt.properties.IntegrationTokenProperties;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Test lives in the same package as {@link ProviderAuthenticationManagerRegistry}
 * so the package-private {@code initialize()} ({@code @PostConstruct}) can be
 * called directly, exactly as Spring would call it.
 */
class ProviderAuthenticationManagerRegistryTest {

    private final AuthProvidersProperties properties = new AuthProvidersProperties();
    private final IntegrationTokenProperties integrationTokenProperties = new IntegrationTokenProperties();
    private final IntegrationTokenSigningKey signingKey = new IntegrationTokenSigningKey();
    private final IntegrationTokenIssuer tokenIssuer =
            new IntegrationTokenIssuer(signingKey, integrationTokenProperties);
    private final IntegrationJwtDecoderConfig decoderConfig =
            new IntegrationJwtDecoderConfig(signingKey, integrationTokenProperties);

    private final ProviderAuthenticationManagerRegistry registry = new ProviderAuthenticationManagerRegistry(
            properties,
            mock(ProviderJwtDecoderFactory.class),
            mock(FederatedJwtAuthenticationConverter.class),
            decoderConfig.integrationJwtDecoder(),
            new IntegrationJwtAuthenticationConverter(),
            integrationTokenProperties
    );

    @Test
    void routesAnIntegrationIssuedTokenToTheIntegrationProvider() {
        registry.initialize();

        IntegrationTokenIssuer.IssuedToken issued = tokenIssuer.issue(
                "ic_test", "dmed", 42L, UUID.randomUUID(), Set.of("form058:submit"));

        AuthenticationManagerResolver<HttpServletRequest> resolver = registry.resolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + issued.tokenValue());

        AuthenticationManager manager = resolver.resolve(request);
        Authentication authenticated = manager.authenticate(new BearerTokenAuthenticationToken(issued.tokenValue()));

        assertThat(authenticated).isInstanceOf(IntegrationClientAuthenticationToken.class);
    }

    @Test
    void rejectsATokenWhoseIssuerMatchesNoRegisteredProvider() {
        registry.initialize();

        // Same signing infrastructure, deliberately different issuer - so this is a
        // well-formed, validly-shaped token that still must not resolve to any
        // provider, since neither the (empty) human-provider config nor the fixed
        // integration issuer matches it.
        IntegrationTokenProperties otherIssuer = new IntegrationTokenProperties();
        otherIssuer.setIssuer("some-other-issuer");
        String unrecognizedIssuerToken = new IntegrationTokenIssuer(signingKey, otherIssuer)
                .issue("ic_test", "dmed", 42L, UUID.randomUUID(), Set.of("form058:submit"))
                .tokenValue();

        AuthenticationManagerResolver<HttpServletRequest> resolver = registry.resolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + unrecognizedIssuerToken);

        assertThatThrownBy(() -> resolver.resolve(request))
                .isInstanceOf(InvalidBearerTokenException.class);
    }
}
