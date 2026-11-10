package uz.uzinfocom.app.platform.security.jwt;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProviderJwtDecoderFactory {

    private final RemoteRsaPublicKeyClient remoteRsaPublicKeyClient;
    private final JwtTokenValidatorFactory validatorFactory;
    private final CacheManager securityCacheManager;
    private final Map<String, JwtDecoder> decodersByProviderKey = new ConcurrentHashMap<>();

    public ProviderJwtDecoderFactory(
            RemoteRsaPublicKeyClient remoteRsaPublicKeyClient,
            JwtTokenValidatorFactory validatorFactory,
            @Qualifier("securityCacheManager") CacheManager securityCacheManager
    ) {
        this.remoteRsaPublicKeyClient = remoteRsaPublicKeyClient;
        this.validatorFactory = validatorFactory;
        this.securityCacheManager = securityCacheManager;
    }

    /**
     * One decoder per provider, shared by every caller. A JWKS decoder keeps the parsed key set in its
     * own field but reads/writes the JWKS through the shared {@code JWKS_BY_URI} cache, so a second
     * decoder instance for the same URI (e.g. one built per login) fills the cache without filling
     * the first decoder's field - and the first one then fails with "JWK Set must not be null".
     */
    public JwtDecoder create(String providerKey, AuthProvidersProperties.ProviderProperties provider) {
        return decodersByProviderKey.computeIfAbsent(providerKey, key -> build(key, provider));
    }

    private JwtDecoder build(String providerKey, AuthProvidersProperties.ProviderProperties provider) {
        if (provider.getSignatureMode() == null) {
            throw new IllegalStateException("Signature mode is not configured for provider: " + providerKey);
        }

        return switch (provider.getSignatureMode()) {
            case PUBLIC_KEY_ENDPOINT -> publicKeyEndpointDecoder(providerKey, provider);
            case JWKS_URI -> jwksDecoder(providerKey, provider);
        };
    }

    private JwtDecoder publicKeyEndpointDecoder(
            String providerKey,
            AuthProvidersProperties.ProviderProperties provider
    ) {
        Cache decoderCache = requireCache(SecurityCacheNames.PUBLIC_KEY_DECODER_BY_PROVIDER);

        return new PublicKeyEndpointJwtDecoder(
                providerKey,
                provider,
                remoteRsaPublicKeyClient,
                validatorFactory,
                decoderCache
        );
    }

    private JwtDecoder jwksDecoder(
            String providerKey,
            AuthProvidersProperties.ProviderProperties provider
    ) {
        if (provider.getJwkSetUri() == null || provider.getJwkSetUri().isBlank()) {
            throw new IllegalStateException("JWK Set URI is not configured for provider: " + providerKey);
        }

        Cache jwksCache = requireCache(SecurityCacheNames.JWKS_BY_URI);

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(provider.getJwkSetUri())
                .cache(jwksCache)
                .build();

        decoder.setJwtValidator(validatorFactory.create(providerKey, provider));

        return decoder;
    }

    private Cache requireCache(String cacheName) {
        Cache cache = securityCacheManager.getCache(cacheName);

        if (cache == null) {
            throw new IllegalStateException("Cache is not configured: " + cacheName);
        }

        return cache;
    }
}