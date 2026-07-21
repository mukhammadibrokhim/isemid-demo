package uz.uzinfocom.app.platform.security.jwt.integration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.security.jwt.properties.IntegrationTokenProperties;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Signs short-lived JWTs for registered integration clients after their
 * client_id/client_secret has already been verified (see
 * {@code IntegrationTokenService} in the {@code integration.inbound.oauth}
 * package). {@code org_id}/{@code org_uuid} are baked in as string claims
 * deliberately — Nimbus's number-claim round-trip can hand back a
 * {@code Long}/{@code Integer}/{@code BigInteger} depending on the code path,
 * a classic {@link ClassCastException} trap on the decode side.
 */
@Component
@RequiredArgsConstructor
public class IntegrationTokenIssuer {

    public static final String ORGANIZATION_ID_CLAIM = "org_id";
    public static final String ORGANIZATION_UUID_CLAIM = "org_uuid";
    public static final String SOURCE_KEY_CLAIM = "source_key";
    public static final String SCOPE_CLAIM = "scope";

    private final IntegrationTokenSigningKey signingKey;
    private final IntegrationTokenProperties properties;

    public IssuedToken issue(
            String clientId,
            String sourceKey,
            Long organizationId,
            UUID organizationUuid,
            Set<String> scopes
    ) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(properties.getTtlSeconds());

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(properties.getIssuer())
                .subject(clientId)
                .claim(SOURCE_KEY_CLAIM, sourceKey)
                .claim(ORGANIZATION_ID_CLAIM, String.valueOf(organizationId))
                .claim(ORGANIZATION_UUID_CLAIM, organizationUuid.toString())
                .claim(SCOPE_CLAIM, String.join(" ", scopes))
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(signingKey.getKeyId())
                .build();

        SignedJWT signedJwt = new SignedJWT(header, claims);

        try {
            signedJwt.sign(new RSASSASigner(signingKey.getPrivateKey()));
        } catch (JOSEException signingFailure) {
            throw new IllegalStateException("Failed to sign integration token", signingFailure);
        }

        return new IssuedToken(signedJwt.serialize(), properties.getTtlSeconds());
    }

    public record IssuedToken(String tokenValue, long expiresInSeconds) {
    }
}
