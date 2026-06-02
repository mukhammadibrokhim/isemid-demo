package uz.uzinfocom.app.platform.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;

@Component
@RequiredArgsConstructor
public class JwtProviderResolver {

    private final AuthProvidersProperties properties;

    public String resolveProviderKey(Jwt jwt) {
        String issuerUri = jwt.getIssuer() == null
                ? null
                : jwt.getIssuer().toString();

        return properties.findProviderKeyByJwtClaims(issuerUri, jwt.getClaims())
                .orElseThrow(() -> new InsufficientAuthenticationException(
                        "No authentication provider is configured for JWT. issuer=" + issuerUri
                ));
    }
}