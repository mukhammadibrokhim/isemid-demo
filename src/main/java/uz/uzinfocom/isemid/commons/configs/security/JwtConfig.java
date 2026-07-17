package uz.uzinfocom.isemid.commons.configs.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import uz.uzinfocom.isemid.commons.security.key.SsoPublicKeyProvider;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private static final String SSO_KEY_ID = "sso-public-key";

    private final SsoPublicKeyProvider ssoPublicKeyProvider;

    @Bean
    public JwtDecoder jwtDecoder() {
        JWKSource<SecurityContext> jwkSource = (jwkSelector, securityContext) ->
                jwkSelector.select(buildJwkSet());

        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        jwtProcessor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource)
        );

        return new NimbusJwtDecoder(jwtProcessor);
    }

    private JWKSet buildJwkSet() throws KeySourceException {
        try {
            RSAPublicKey publicKey = ssoPublicKeyProvider.getPublicKey();

            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .keyID(SSO_KEY_ID)
                    .algorithm(JWSAlgorithm.RS256)
                    .build();

            return new JWKSet(List.of(rsaKey));
        } catch (Exception ex) {
            throw new KeySourceException("Failed to build SSO JWT verification key set", ex);
        }
    }
}