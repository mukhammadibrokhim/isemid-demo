package uz.uzinfocom.app.platform.security.jwt.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import uz.uzinfocom.app.platform.security.jwt.properties.IntegrationTokenProperties;

/**
 * Verifies the self-issued integration JWTs against the same in-memory
 * keypair {@link IntegrationTokenIssuer} signs with. Deliberately NOT routed
 * through {@code ProviderJwtDecoderFactory} — that factory is entirely
 * shaped around fetching a key over HTTP ({@code PUBLIC_KEY_ENDPOINT}/
 * {@code JWKS_URI}), the wrong fit for a same-JVM self-issued key (would mean
 * a pointless loopback HTTP call on every cache miss).
 */
@Configuration
@RequiredArgsConstructor
public class IntegrationJwtDecoderConfig {

    private final IntegrationTokenSigningKey signingKey;
    private final IntegrationTokenProperties properties;

    @Bean("integrationJwtDecoder")
    public JwtDecoder integrationJwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(signingKey.getPublicKey()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getIssuer()));
        return decoder;
    }
}
