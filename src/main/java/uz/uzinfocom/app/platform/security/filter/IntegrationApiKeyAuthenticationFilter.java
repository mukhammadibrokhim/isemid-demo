package uz.uzinfocom.app.platform.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.context.SecurityHeaders;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

import java.io.IOException;
import java.util.Optional;

/**
 * Authenticates an {@code authType=API_KEY} {@link IntegrationClient} from
 * the {@code X-Api-Key} header, format {@code <clientId>.<token>} (the
 * {@code clientId} prefix makes the client identifiable by an indexed
 * lookup instead of a full-table BCrypt scan — see {@code
 * IntegrationClientCommandService#issueCredential}). Self-scoped to {@code
 * /integration/v1/**} rather than a dedicated {@code SecurityFilterChain},
 * since {@code SecurityConfig} keeps {@code httpBasic}/custom header auth
 * disabled for every other route.
 */
@Component
@RequiredArgsConstructor
public class IntegrationApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final IntegrationClientRepository integrationClientRepository;
    private final PasswordEncoder integrationClientPasswordEncoder;
    private final IntegrationCredentialAuthenticator credentialAuthenticator;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return SecurityContextHolder.getContext().getAuthentication() != null
                || !request.getRequestURI().startsWith(ApiPaths.Integration.ROOT)
                || !StringUtils.hasText(request.getHeader(SecurityHeaders.INTEGRATION_API_KEY));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        resolveClient(request.getHeader(SecurityHeaders.INTEGRATION_API_KEY))
                .ifPresent(credentialAuthenticator::authenticate);

        filterChain.doFilter(request, response);
    }

    private Optional<IntegrationClient> resolveClient(String apiKey) {
        int separator = apiKey.lastIndexOf('.');
        if (separator < 0) {
            return Optional.empty();
        }

        String clientId = apiKey.substring(0, separator);
        String token = apiKey.substring(separator + 1);

        return integrationClientRepository.findByClientId(clientId)
                .filter(IntegrationClient::isActive)
                .filter(client -> client.getAuthType() == IntegrationAuthType.API_KEY)
                .filter(client -> integrationClientPasswordEncoder.matches(token, client.getApiKeyHash()));
    }
}
