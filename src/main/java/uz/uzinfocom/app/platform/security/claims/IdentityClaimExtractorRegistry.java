package uz.uzinfocom.app.platform.security.claims;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class IdentityClaimExtractorRegistry {

    private final Map<String, IdentityClaimExtractor> extractorsByProvider;

    public IdentityClaimExtractorRegistry(List<IdentityClaimExtractor> extractors) {
        this.extractorsByProvider = extractors.stream()
                .collect(Collectors.toUnmodifiableMap(
                        IdentityClaimExtractor::providerKey,
                        Function.identity()
                ));
    }

    public ExternalIdentityPayload extract(String providerKey, Jwt jwt) {
        IdentityClaimExtractor extractor = extractorsByProvider.get(providerKey);

        if (extractor == null) {
            throw new InsufficientAuthenticationException(
                    "No claim extractor configured for provider: " + providerKey
            );
        }

        return extractor.extract(jwt);
    }
}