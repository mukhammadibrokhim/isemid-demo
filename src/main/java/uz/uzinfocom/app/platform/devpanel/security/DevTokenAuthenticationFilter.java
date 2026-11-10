package uz.uzinfocom.app.platform.devpanel.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

import java.io.IOException;
import java.util.Optional;

/**
 * Resolves the dev-panel caller from {@code Authorization: Bearer <token>}.
 * Any problem (missing/forged/expired token, unknown or disabled account,
 * stale credential version) simply leaves the request unauthenticated, so the
 * chain's entry point answers 401 - the response never says which check failed.
 *
 * <p>The only other credential accepted is a single-use {@code ?ticket=}, and
 * only on {@code GET}s of the SSE stream endpoints (an {@code EventSource}
 * cannot set headers). A ticket is only ever issued to a fully verified session.
 */
@Component
@RequiredArgsConstructor
public class DevTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final RequestMatcher STREAM_ENDPOINTS = new OrRequestMatcher(
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, ApiPaths.Dev.ROOT + ApiPaths.Dev.METRICS_STREAM),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, ApiPaths.Dev.ROOT + ApiPaths.Dev.ACTUATOR_STREAM_ENDPOINT),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, ApiPaths.Dev.ROOT + ApiPaths.Dev.ACTUATOR_STREAM_ENDPOINT_PATH)
    );

    private final DevTokenService tokenService;
    private final DevPrincipalLoader principalLoader;

    /**
     * Boot also registers this {@code @Component} as a plain servlet filter;
     * keep that registration inert for everything outside the dev panel,
     * whose Bearer tokens are SSO/DHP JWTs this filter must never look at.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return true;
        }
        // getRequestURI() includes the servlet context path (e.g. /api on the deployed
        // servers); the dev-panel root is declared relative to it.
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return !path.startsWith(ApiPaths.Dev.ROOT);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            tokenService.parse(header.substring(BEARER_PREFIX.length()).trim()).ifPresent(claims -> {
                DevUserPrincipal principal = principalLoader.load(claims.devUserId());
                if (principal != null
                        && principal.isEnabled()
                        && principal.getCredentialVersion().equals(claims.credentialVersion())) {
                    SecurityContextHolder.getContext().setAuthentication(
                            new DevAuthenticationToken(principal, claims.mfaVerified()));
                }
            });
            return;
        }

        String ticket = request.getParameter("ticket");
        if (ticket != null && STREAM_ENDPOINTS.matches(request)) {
            Optional<Long> devUserId = tokenService.redeemTicket(ticket);
            devUserId.map(principalLoader::load)
                    .filter(DevUserPrincipal::isEnabled)
                    .ifPresent(principal -> SecurityContextHolder.getContext()
                            .setAuthentication(new DevAuthenticationToken(principal, true)));
        }
    }
}
