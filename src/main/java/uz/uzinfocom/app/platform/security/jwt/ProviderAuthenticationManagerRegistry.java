package uz.uzinfocom.app.platform.security.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.security.auth.FederatedJwtAuthenticationConverter;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderAuthenticationManagerRegistry {

    public static final String REQUEST_PROVIDER_KEY_ATTRIBUTE =
            ProviderAuthenticationManagerRegistry.class.getName() + ".PROVIDER_KEY";

    private final AuthProvidersProperties properties;
    private final ProviderJwtDecoderFactory decoderFactory;
    private final FederatedJwtAuthenticationConverter converter;

    private final BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

    private Map<String, AuthenticationManager> authenticationManagersByProviderKey = Map.of();

    @PostConstruct
    void initialize() {
        LinkedHashMap<String, AuthenticationManager> managers = new LinkedHashMap<>();

        properties.getProviders().forEach((providerKey, provider) -> {
            if (provider == null || !provider.isEnabled()) {
                log.info("Authentication provider skipped. providerKey={}, reason=disabled", providerKey);
                return;
            }

            JwtAuthenticationProvider authenticationProvider = new JwtAuthenticationProvider(
                    decoderFactory.create(providerKey, provider)
            );

            authenticationProvider.setJwtAuthenticationConverter(converter);

            managers.put(providerKey, authenticationProvider::authenticate);

            log.info(
                    "Authentication provider registered. providerKey={}, issuerUri={}, signatureMode={}, validateIssuer={}, missingIssuerMarkerClaim={}",
                    providerKey,
                    provider.getIssuerUri(),
                    provider.getSignatureMode(),
                    provider.isValidateIssuer(),
                    provider.getMissingIssuerMarkerClaim()
            );
        });

        this.authenticationManagersByProviderKey = Map.copyOf(managers);
    }

    public AuthenticationManagerResolver<HttpServletRequest> resolver() {
        return request -> {
            String token = bearerTokenResolver.resolve(request);

            if (!StringUtils.hasText(token)) {
                throw new InvalidBearerTokenException("Bearer token is missing");
            }

            String providerKey = resolveProviderKey(token);

            AuthenticationManager authenticationManager = authenticationManagersByProviderKey.get(providerKey);

            if (authenticationManager == null) {
                throw new InvalidBearerTokenException("Authentication provider is not registered: " + providerKey);
            }

            request.setAttribute(REQUEST_PROVIDER_KEY_ATTRIBUTE, providerKey);

            log.debug(
                    "JWT routed to authentication provider. providerKey={}, method={}, path={}",
                    providerKey,
                    request.getMethod(),
                    request.getRequestURI()
            );

            return authenticationManager;
        };
    }

    private String resolveProviderKey(String token) {
        JWTClaimsSet claims = parseClaims(token);

        String issuer = normalizeIssuer(claims.getIssuer());

        if (StringUtils.hasText(issuer)) {
            return properties.findProviderKeyByIssuer(issuer)
                    .orElseThrow(() -> {
                        log.warn("JWT issuer is not registered. issuer={}", issuer);
                        return new InvalidBearerTokenException("Unknown token issuer");
                    });
        }

        Map<String, Object> claimMap = claims.getClaims();

        return properties.findProviderKeyByMissingIssuerMarkerClaim(claimMap)
                .orElseThrow(() -> {
                    log.warn("JWT issuer is missing and no missing-issuer marker claim matched");
                    return new InvalidBearerTokenException("Missing token issuer");
                });
    }

    private JWTClaimsSet parseClaims(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet();
        } catch (ParseException exception) {
            throw new InvalidBearerTokenException("Invalid JWT format");
        }
    }

    private String normalizeIssuer(String issuer) {
        if (!StringUtils.hasText(issuer)) {
            return null;
        }

        String normalized = issuer.trim();

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}