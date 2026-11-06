package uz.uzinfocom.app.platform.security.jwt;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;
import uz.uzinfocom.app.platform.security.properties.ProviderSignatureMode;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * LoginHistoryRecorder asks the factory for a decoder on every login. Spring's JWKS decoder only
 * parses the shared cache entry when it is constructed, so a second decoder that fills the cache
 * used to leave the long-lived one without a key set ("JWK Set must not be null" -> 401).
 */
class ProviderJwtDecoderFactoryTest {

    private static final String ISSUER = "https://issuer.test/sso";

    private HttpServer jwksServer;
    private RSAKey key;
    private ProviderJwtDecoderFactory factory;
    private AuthProvidersProperties.ProviderProperties provider;

    @BeforeEach
    void setUp() throws Exception {
        key = new RSAKeyGenerator(2048).keyID("test-key").generate();

        byte[] jwks = new JWKSet(key.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
        jwksServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        jwksServer.createContext("/jwks.json", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jwks.length);
            exchange.getResponseBody().write(jwks);
            exchange.close();
        });
        jwksServer.start();

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(java.util.List.of(
                new CaffeineCache(SecurityCacheNames.JWKS_BY_URI,
                        Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(2)).build())
        ));
        cacheManager.initializeCaches();

        factory = new ProviderJwtDecoderFactory(
                mock(RemoteRsaPublicKeyClient.class),
                new JwtTokenValidatorFactory(),
                cacheManager
        );

        provider = new AuthProvidersProperties.ProviderProperties();
        provider.setEnabled(true);
        provider.setSignatureMode(ProviderSignatureMode.JWKS_URI);
        provider.setJwkSetUri("http://127.0.0.1:" + jwksServer.getAddress().getPort() + "/jwks.json");
        provider.setIssuerUri(ISSUER);
    }

    @AfterEach
    void tearDown() {
        jwksServer.stop(0);
    }

    private String token() throws Exception {
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("test-key").build(),
                new JWTClaimsSet.Builder()
                        .issuer(ISSUER)
                        .subject("user")
                        .issueTime(new Date())
                        .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                        .build()
        );
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }

    @Test
    void sameDecoderIsReturnedForTheSameProvider() {
        assertThat(factory.create("dhp", provider)).isSameAs(factory.create("dhp", provider));
    }

    @Test
    void decoderStillWorksAfterAnotherCallerHasPrimedTheSharedJwksCache() throws Exception {
        String token = token();

        JwtDecoder registryDecoder = factory.create("dhp", provider);
        JwtDecoder loginHistoryDecoder = factory.create("dhp", provider);

        // login-time decode fills the shared JWKS cache first ...
        assertThat(loginHistoryDecoder.decode(token).getSubject()).isEqualTo("user");
        // ... then the request-time decode must not see an empty key set.
        assertThat(registryDecoder.decode(token).getSubject()).isEqualTo("user");
    }
}
