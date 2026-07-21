package uz.uzinfocom.app.platform.security.jwt.integration;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import uz.uzinfocom.app.platform.security.jwt.properties.IntegrationTokenProperties;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationTokenIssuerTest {

    private final IntegrationTokenSigningKey signingKey = new IntegrationTokenSigningKey();
    private final IntegrationTokenProperties properties = new IntegrationTokenProperties();
    private final IntegrationTokenIssuer issuer = new IntegrationTokenIssuer(signingKey, properties);

    @Test
    void issuedTokenClaimsRoundTripThroughANewlyBuiltDecoder() {
        UUID organizationUuid = UUID.randomUUID();

        IntegrationTokenIssuer.IssuedToken issued = issuer.issue(
                "ic_test", "dmed", 42L, organizationUuid, Set.of("form058:submit", "form0581:submit"));

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(signingKey.getPublicKey()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getIssuer()));

        Jwt jwt = decoder.decode(issued.tokenValue());

        // getClaimAsString, not the getIssuer() convenience getter - getIssuer() insists
        // on parsing the claim as a URL, which our plain "isemid-integration" issuer string
        // is not (production code reads it the same way, via Nimbus's raw string claim).
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(properties.getIssuer());
        assertThat(jwt.getSubject()).isEqualTo("ic_test");
        assertThat(jwt.getClaimAsString(IntegrationTokenIssuer.SOURCE_KEY_CLAIM)).isEqualTo("dmed");
        // Baked in as strings deliberately - Nimbus's number-claim round-trip can hand back
        // Long/Integer/BigInteger depending on the code path, so decoding must not assume a numeric type.
        assertThat(jwt.getClaimAsString(IntegrationTokenIssuer.ORGANIZATION_ID_CLAIM)).isEqualTo("42");
        assertThat(jwt.getClaimAsString(IntegrationTokenIssuer.ORGANIZATION_UUID_CLAIM)).isEqualTo(organizationUuid.toString());
        assertThat(jwt.getClaimAsString(IntegrationTokenIssuer.SCOPE_CLAIM))
                .contains("form058:submit", "form0581:submit");
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
        assertThat(issued.expiresInSeconds()).isEqualTo(properties.getTtlSeconds());
    }
}
