package uz.uzinfocom.app.platform.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.*;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth")
public class AuthProvidersProperties {

    private Map<String, ProviderProperties> providers = new LinkedHashMap<>();

    public Optional<String> findProviderKeyByIssuer(String issuerUri) {
        String normalizedIssuer = normalizeIssuer(issuerUri);

        return providers.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> entry.getValue().isEnabled())
                .filter(entry -> normalizedIssuer != null)
                .filter(entry -> normalizedIssuer.equals(normalizeIssuer(entry.getValue().getIssuerUri())))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public Optional<String> findProviderKeyByMissingIssuerMarkerClaim(Map<String, Object> claims) {
        if (claims == null || claims.isEmpty()) {
            return Optional.empty();
        }

        String matchedProviderKey = null;

        for (Map.Entry<String, ProviderProperties> entry : providers.entrySet()) {
            ProviderProperties provider = entry.getValue();

            if (provider == null || !provider.isEnabled()) {
                continue;
            }

            String markerClaim = provider.getMissingIssuerMarkerClaim();

            if (!StringUtils.hasText(markerClaim)) {
                continue;
            }

            if (!claims.containsKey(markerClaim)) {
                continue;
            }

            if (matchedProviderKey != null) {
                throw new IllegalStateException(
                        "JWT without issuer matched multiple providers: "
                                + matchedProviderKey + ", " + entry.getKey()
                );
            }

            matchedProviderKey = entry.getKey();
        }

        return Optional.ofNullable(matchedProviderKey);
    }

    public ProviderProperties requireProvider(String providerKey) {
        ProviderProperties provider = providers.get(providerKey);

        if (provider == null || !provider.isEnabled()) {
            throw new IllegalStateException("Authentication provider is not configured or disabled: " + providerKey);
        }

        return provider;
    }

    public Optional<String> findProviderKeyByJwtClaims(
            String issuerUri,
            Map<String, Object> claims
    ) {
        String normalizedIssuer = normalizeIssuer(issuerUri);

        if (StringUtils.hasText(normalizedIssuer)) {
            return findProviderKeyByIssuer(normalizedIssuer);
        }

        return findProviderKeyByMissingIssuerMarkerClaim(claims);
    }

    private static String normalizeIssuer(String issuerUri) {
        if (!StringUtils.hasText(issuerUri)) {
            return null;
        }

        String normalized = issuerUri.trim();

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    @Getter
    @Setter
    public static class ProviderProperties {
        private boolean enabled;
        private String authBaseDomain;
        private String fhirBaseDomain;
        private String iamApiBaseDomain;
        private String issuerUri;
        private ProviderSignatureMode signatureMode;
        private String jwkSetUri;
        private String publicKeyUri;
        private boolean validateIssuer = true;
        private boolean validateAudience = false;
        private List<String> audiences = new ArrayList<>();
        private String missingIssuerMarkerClaim;
        private IamProperties iam = new IamProperties();
    }

    @Getter
    @Setter
    public static class IamProperties {

        private String organizationUrlTemplate;

        private String practitionerUrlTemplate;
    }
}