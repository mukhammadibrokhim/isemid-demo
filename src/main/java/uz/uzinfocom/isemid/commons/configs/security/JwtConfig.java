package uz.uzinfocom.isemid.commons.configs.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;

import static uz.uzinfocom.isemid.commons.utils.RSAKeyUtils.publicKey;
import static uz.uzinfocom.isemid.commons.utils.RSAKeyUtils.publicKeyInternal;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        RSAPublicKey key1 = publicKey();
        RSAPublicKey key2 = publicKeyInternal();

        List<JWK> keys = Arrays.asList(
                new RSAKey.Builder(key1).build(),
                new RSAKey.Builder(key2).build()
        );

        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(keys));

        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        JWSVerificationKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);

        jwtProcessor.setJWSKeySelector(keySelector);

        return new NimbusJwtDecoder(jwtProcessor);
    }
}
