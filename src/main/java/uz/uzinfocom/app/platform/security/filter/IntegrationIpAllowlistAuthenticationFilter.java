package uz.uzinfocom.app.platform.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.context.SecurityHeaders;
import uz.uzinfocom.app.platform.security.ip.IpAllowlistMatcher;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Authenticates an {@code authType=IP_ALLOWLIST} {@link IntegrationClient}
 * with no credential at all — identity comes purely from {@code
 * request.getRemoteAddr()} matching that client's {@code allowedIps}
 * (mandatory and non-empty for this auth type, see {@code
 * IntegrationClientCommandService}). Only runs when the request carries
 * neither an {@code X-Api-Key} header nor an {@code Authorization} header,
 * so it never shadows the other credential kinds. Exactly one matching
 * active client authenticates the request; zero or more than one leaves it
 * unauthenticated (falls through to the normal 401) — an ambiguous IP match
 * must never be resolved by guessing.
 */
@Component
@RequiredArgsConstructor
public class IntegrationIpAllowlistAuthenticationFilter extends OncePerRequestFilter {

    private final IntegrationClientRepository integrationClientRepository;
    private final IntegrationCredentialAuthenticator credentialAuthenticator;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return SecurityContextHolder.getContext().getAuthentication() != null
                || !request.getRequestURI().startsWith(ApiPaths.Integration.ROOT)
                || StringUtils.hasText(request.getHeader(SecurityHeaders.INTEGRATION_API_KEY))
                || StringUtils.hasText(request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        resolveClient(request.getRemoteAddr()).ifPresent(credentialAuthenticator::authenticate);

        filterChain.doFilter(request, response);
    }

    private Optional<IntegrationClient> resolveClient(String remoteAddress) {
        List<IntegrationClient> candidates = integrationClientRepository
                .findAllByAuthTypeAndActiveTrue(IntegrationAuthType.IP_ALLOWLIST).stream()
                .filter(client -> StringUtils.hasText(client.getAllowedIps()))
                .filter(client -> IpAllowlistMatcher.isAllowed(remoteAddress, client.getAllowedIps()))
                .toList();

        return candidates.size() == 1 ? Optional.of(candidates.get(0)) : Optional.empty();
    }
}
