package uz.uzinfocom.app.platform.security.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uz.uzinfocom.app.platform.security.jwt.integration.IntegrationTokenIssuer;
import uz.uzinfocom.app.platform.security.principal.IntegrationClientPrincipal;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationJwtAuthenticationConverterTest {

    private final IntegrationJwtAuthenticationConverter converter = new IntegrationJwtAuthenticationConverter();

    @Test
    void buildsIntegrationClientTokenFromClaimsWithoutTouchingIamSync() {
        UUID organizationUuid = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuer("isemid-integration")
                .subject("ic_test")
                .claim(IntegrationTokenIssuer.ORGANIZATION_ID_CLAIM, "42")
                .claim(IntegrationTokenIssuer.ORGANIZATION_UUID_CLAIM, organizationUuid.toString())
                .claim(IntegrationTokenIssuer.SCOPE_CLAIM, "form058:submit form0581:submit")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        AbstractAuthenticationToken result = converter.convert(jwt);

        assertThat(result).isInstanceOf(IntegrationClientAuthenticationToken.class);
        IntegrationClientPrincipal principal = (IntegrationClientPrincipal) result.getPrincipal();
        assertThat(principal.clientId()).isEqualTo("ic_test");
        assertThat(principal.organizationId()).isEqualTo(42L);
        assertThat(principal.organizationUuid()).isEqualTo(organizationUuid);
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("SCOPE_form058:submit", "SCOPE_form0581:submit");
        assertThat(result.isAuthenticated()).isTrue();
    }
}
