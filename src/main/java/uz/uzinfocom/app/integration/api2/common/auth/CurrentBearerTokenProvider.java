package uz.uzinfocom.app.integration.api2.common.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.integration.api2.common.exception.Api2MissingBearerTokenException;

import java.util.Optional;

@Component
public class CurrentBearerTokenProvider {

    public Optional<String> currentToken() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        switch (authentication) {
            case null -> {
                return Optional.empty();
            }
            case JwtAuthenticationToken jwtAuthenticationToken -> {
                return tokenValue(jwtAuthenticationToken.getToken().getTokenValue());
            }
            case BearerTokenAuthentication bearerTokenAuthentication -> {
                return tokenValue(bearerTokenAuthentication.getToken().getTokenValue());
            }
            default -> {
            }
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return tokenValue(jwt.getTokenValue());
        }

        if (authentication.getCredentials() instanceof String token) {
            return tokenValue(token);
        }

        return Optional.empty();
    }

    public String getRequiredToken() {
        return currentToken()
                .orElseThrow(Api2MissingBearerTokenException::new);
    }

    private Optional<String> tokenValue(String token) {
        return StringUtils.hasText(token)
                ? Optional.of(token.trim())
                : Optional.empty();
    }
}
