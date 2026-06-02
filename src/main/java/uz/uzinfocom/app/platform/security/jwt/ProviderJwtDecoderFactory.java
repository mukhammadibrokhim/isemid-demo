package uz.uzinfocom.app.platform.security.jwt;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;

@Component
public class ProviderJwtDecoderFactory {

    private final RemoteRsaPublicKeyClient remoteRsaPublicKeyClient;
    private final JwtTokenValidatorFactory validatorFactory;
    private final CacheManager securityCacheManager;

    public ProviderJwtDecoderFactory(
            RemoteRsaPublicKeyClient remoteRsaPublicKeyClient,
            JwtTokenValidatorFactory validatorFactory,
            @Qualifier("securityCacheManager") CacheManager securityCacheManager
    ) {
        this.remoteRsaPublicKeyClient = remoteRsaPublicKeyClient;
        this.validatorFactory = validatorFactory;
        this.securityCacheManager = securityCacheManager;
    }

    public JwtDecoder create(String providerKey, AuthProvidersProperties.ProviderProperties provider) {
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