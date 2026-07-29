package uz.uzinfocom.app.platform.security.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.settings.application.RouteAccessPolicyResolver;

import java.util.function.Supplier;

/**
 * Replaces the old {@code .requestMatchers(SecurityRouteCatalog.OPEN_PATTERNS...).permitAll()}
 * + final {@code .anyRequest().authenticated()} pair in {@link SecurityConfig}. Those
 * were compiled into an immutable authorization chain once, at {@code http.build()} time
 * - there was no supported way to change which paths were public without a restart.
 * This manager instead re-resolves the request path against the DB-backed
 * {@link RouteAccessPolicyResolver} on every call, exactly like
 * {@code @adminAccessGuard.isAdmin()} is already re-evaluated per request for the
 * admin-loggers rule a few lines above it in {@code SecurityConfig}.
 *
 * <p>Fail-closed by design: any path with no matching row (including a completely
 * empty table, e.g. if the seed migration were ever rolled back) requires
 * authentication - identical to today's {@code RequestPolicy.defaultProtectedRoute()}.
 * A misconfigured or empty policy table can make the app stricter than intended,
 * never more permissive.
 */
@Component
@RequiredArgsConstructor
public class DynamicRouteAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final RouteAccessPolicyResolver routeAccessPolicyResolver;

    /**
     * Spring Security installs an {@code AnonymousAuthenticationFilter} by default,
     * which gives every unauthenticated request a non-null {@code AnonymousAuthenticationToken}
     * whose {@code isAuthenticated()} is {@code true} - that's how anonymous requests flow
     * through the filter chain at all. A naive {@code authentication.get().isAuthenticated()}
     * check here would therefore grant anonymous callers access to every non-open route.
     * Delegating to Spring's own {@code AuthenticatedAuthorizationManager.authenticated()} -
     * the same manager {@code .anyRequest().authenticated()} used internally - correctly
     * excludes anonymous/remember-me-only authentication instead of re-implementing that
     * distinction by hand.
     */
    private final AuthorizationManager<RequestAuthorizationContext> authenticatedManager =
            AuthenticatedAuthorizationManager.authenticated();

    @Override
    public AuthorizationResult authorize(
            Supplier<? extends Authentication> authentication,
            RequestAuthorizationContext context
    ) {
        boolean open = routeAccessPolicyResolver.resolve(path(context.getRequest())).publicRoute();

        if (open) {
            return new AuthorizationDecision(true);
        }

        return authenticatedManager.authorize(authentication, context);
    }

    private String path(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return StringUtils.hasText(requestUri) ? requestUri : "/";
    }
}
