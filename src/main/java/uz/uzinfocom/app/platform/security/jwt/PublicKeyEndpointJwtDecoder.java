package uz.uzinfocom.app.platform.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;

@Slf4j
public class PublicKeyEndpointJwtDecoder implements JwtDecoder {

    private final String providerKey;
    private final AuthProvidersProperties.ProviderProperties provider;
    private final RemoteRsaPublicKeyClient remoteRsaPublicKeyClient;
    private final JwtTokenValidatorFactory validatorFactory;
    private final Cache decoderCache;

    public PublicKeyEndpointJwtDecoder(
            String providerKey,
            AuthProvidersProperties.ProviderProperties provider,
            RemoteRsaPublicKeyClient remoteRsaPublicKeyClient,
            JwtTokenValidatorFactory validatorFactory,
            Cache decoderCache
    ) {
        this.providerKey = providerKey;
        this.provider = provider;
        this.remoteRsaPublicKeyClient = remoteRsaPublicKeyClient;
        this.validatorFactory = validatorFactory;
        this.decoderCache = decoderCache;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            return getOrCreateDecoder().decode(token);
        } catch (JwtValidationException validationException) {
            /*
             * Validation errors are not public-key cache problems.
             *
             * Examples:
             * - expired token
             * - invalid audience
             * - invalid issuer
             * - nbf/iat validation problem
             *
             * Do not evict decoder cache here.
             */
            throw validationException;
        } catch (JwtException decodeException) {
            /*
             * Decode/signature errors may happen when SSO rotated its public key.
             * We evict cached decoder and retry only once.
             */
            if (!shouldRefreshPublicKey(decodeException)) {
                throw decodeException;
            }

            log.warn(
                    "JWT decode failed, refreshing public-key decoder. providerKey={}, reason={}",
                    providerKey,
                    decodeException.getMessage()
            );

            decoderCache.evict(providerKey);

            return getOrCreateDecoder().decode(token);
        }
    }

    private NimbusJwtDecoder getOrCreateDecoder() {
        return decoderCache.get(providerKey, this::buildDecoder);
    }

    private NimbusJwtDecoder buildDecoder() {
        long startedAt = System.nanoTime();

        try {
            log.info("Building JWT decoder from remote public key. providerKey={}, publicKeyUri={}",
                    providerKey,
                    provider.getPublicKeyUri()
            );

            NimbusJwtDecoder decoder = NimbusJwtDecoder
                    .withPublicKey(remoteRsaPublicKeyClient.fetch(provider.getPublicKeyUri()))
                    .build();

            decoder.setJwtValidator(validatorFactory.create(providerKey, provider));

            return decoder;
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;

            log.info("JWT decoder build completed. providerKey={}, durationMs={}",
                    providerKey,
                    durationMs
            );
        }
    }

    private boolean shouldRefreshPublicKey(JwtException exception) {
        String message = exception.getMessage();

        if (!StringUtils.hasText(message)) {
            return false;
        }

        String normalized = message.toLowerCase();

        return normalized.contains("invalid signature")
                || normalized.contains("signature verification failed")
                || normalized.contains("signed jwt rejected")
                || normalized.contains("another algorithm expected")
                || normalized.contains("jws verification failed");
    }
}