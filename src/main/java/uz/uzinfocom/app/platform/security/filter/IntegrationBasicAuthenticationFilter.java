package uz.uzinfocom.app.platform.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * Authenticates an {@code authType=BASIC_AUTH} {@link IntegrationClient} from
 * a standard {@code Authorization: Basic <base64(clientId:secret)>} header —
 * {@code clientId} as username. Parses the header itself rather than using
 * Spring's {@code httpBasic()} DSL, which is app-wide and would enable Basic
 * Auth on every route; self-scoped to {@code /integration/v1/**} instead,
 * same as the sibling API-key and IP-allow-list filters.
 */
@Component
@RequiredArgsConstructor
public class IntegrationBasicAuthenticationFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX = "Basic ";

    private final IntegrationClientRepository integrationClientRepository;
    private final PasswordEncoder integrationClientPasswordEncoder;
    private final IntegrationCredentialAuthenticator credentialAuthenticator;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        return SecurityContextHolder.getContext().getAuthentication() != null
                || !request.getRequestURI().startsWith(ApiPaths.Integration.ROOT)
                || !StringUtils.hasText(header)
                || !header.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX.length());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        resolveClient(request.getHeader(HttpHeaders.AUTHORIZATION))
                .ifPresent(credentialAuthenticator::authenticate);

        filterChain.doFilter(request, response);
    }

    private Optional<IntegrationClient> resolveClient(String header) {
        String[] credentials = decode(header.substring(BASIC_PREFIX.length()));
        if (credentials == null) {
            return Optional.empty();
        }

        String clientId = credentials[0];
        String secret = credentials[1];

        return integrationClientRepository.findByClientId(clientId)
                .filter(IntegrationClient::isActive)
                .filter(client -> client.getAuthType() == IntegrationAuthType.BASIC_AUTH)
                .filter(client -> integrationClientPasswordEncoder.matches(secret, client.getBasicAuthSecretHash()));
    }

    private String[] decode(String base64Credentials) {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Credentials);
        } catch (IllegalArgumentException malformedBase64) {
            return null;
        }

        String decodedText = new String(decoded, StandardCharsets.UTF_8);
        int separator = decodedText.indexOf(':');
        if (separator < 0) {
            return null;
        }

        return new String[] {decodedText.substring(0, separator), decodedText.substring(separator + 1)};
    }
}
