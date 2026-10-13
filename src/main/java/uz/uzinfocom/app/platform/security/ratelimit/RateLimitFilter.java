package uz.uzinfocom.app.platform.security.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.platform.security.principal.IntegrationClientPrincipal;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window request budget for the endpoints listed in
 * {@link RateLimitProperties}. Wired into the main chain just before
 * {@code AuthorizationFilter}, so the bearer token has already been resolved
 * and the identity-lookup budget can be charged to the user rather than to a
 * shared NAT/proxy IP. Anonymous callers are keyed by
 * {@code request.getRemoteAddr()}, which is only trustworthy because
 * {@code server.forward-headers-strategy=native} lets Tomcat's
 * {@code RemoteIpValve} accept {@code X-Forwarded-For} from trusted proxies only.
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long MAX_TRACKED_KEYS = 100_000;

    private final RateLimitProperties properties;
    private final ErrorResponseWriter errorResponseWriter;
    private final Cache<String, AtomicInteger> authCounters;
    private final Cache<String, AtomicInteger> integrationTokenCounters;
    private final Cache<String, AtomicInteger> identityLookupCounters;
    private final Cache<String, AtomicInteger> integrationApiCounters;

    public RateLimitFilter(RateLimitProperties properties, ErrorResponseWriter errorResponseWriter) {
        this.properties = properties;
        this.errorResponseWriter = errorResponseWriter;
        this.authCounters = counters(properties.getAuth());
        this.integrationTokenCounters = counters(properties.getIntegrationToken());
        this.identityLookupCounters = counters(properties.getIdentityLookup());
        this.integrationApiCounters = counters(properties.getIntegrationApi());
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !properties.isEnabled() || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (isUnder(path, ApiPaths.Auth.ROOT)) {
            if (!tryAcquire(authCounters, properties.getAuth(), "ip:" + request.getRemoteAddr(), response)) {
                reject(request, response, "auth");
                return;
            }
        } else if (path.equals(ApiPaths.Integration.OAUTH_TOKEN)) {
            if (!tryAcquire(integrationTokenCounters, properties.getIntegrationToken(),
                    "ip:" + request.getRemoteAddr(), response)) {
                reject(request, response, "integration-token");
                return;
            }
        } else if (isUnder(path, ApiPaths.Citizen.ROOT) || isUnder(path, ApiPaths.LegalEntity.ROOT)) {
            if (!tryAcquire(identityLookupCounters, properties.getIdentityLookup(), callerKey(request), response)) {
                reject(request, response, "identity-lookup");
                return;
            }
        } else if (isUnder(path, ApiPaths.Integration.ROOT)) {
            if (!tryAcquire(integrationApiCounters, properties.getIntegrationApi(), callerKey(request), response)) {
                reject(request, response, "integration-api");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static Cache<String, AtomicInteger> counters(RateLimitProperties.Rule rule) {
        return Caffeine.newBuilder()
                .expireAfterWrite(rule.getWindow())
                .maximumSize(MAX_TRACKED_KEYS)
                .build();
    }

    /**
     * The counter is created on the first request of a window and expires
     * {@code window} after that write, so each key gets {@code limit}
     * requests per window measured from its first request.
     */
    private static boolean tryAcquire(
            Cache<String, AtomicInteger> counters,
            RateLimitProperties.Rule rule,
            String key,
            HttpServletResponse response
    ) {
        AtomicInteger counter = counters.get(key, ignored -> new AtomicInteger());
        if (counter.incrementAndGet() <= rule.getLimit()) {
            return true;
        }

        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds(rule.getWindow())));
        return false;
    }

    private static long retryAfterSeconds(Duration window) {
        return Math.max(1, window.toSeconds());
    }

    private static String callerKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            if (authentication.getPrincipal() instanceof IntegrationClientPrincipal client) {
                return "client:" + client.clientId();
            }
            return "user:" + authentication.getName();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private static boolean isUnder(String path, String root) {
        return path.equals(root) || path.startsWith(root + "/");
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String rule) throws IOException {
        log.warn("Rate limit exceeded. rule={}, remoteAddr={}, path={}", rule, request.getRemoteAddr(), request.getRequestURI());
        errorResponseWriter.write(request, response, HttpStatus.TOO_MANY_REQUESTS, ErrorCode.TOO_MANY_REQUESTS);
    }
}
