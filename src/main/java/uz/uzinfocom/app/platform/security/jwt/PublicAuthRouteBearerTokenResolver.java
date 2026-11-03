package uz.uzinfocom.app.platform.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

/**
 * Ignores any {@code Authorization: Bearer} header on the login-proxy surface
 * ({@code /v1/auth/**}), which is public (see the {@code /v1/auth/**} row of
 * {@code RouteAccessPolicy}) and takes its tokens from the request body.
 *
 * <p>Without this, a client that attaches its session token to every request
 * (a typical HTTP interceptor) makes {@code BearerTokenAuthenticationFilter}
 * authenticate that token before the controller runs - including the IAM
 * user/role sync, which rejects a token carrying no {@code isemid_*} role with a
 * 403. {@code /logout} and {@code /refresh} would then be unusable for exactly
 * the users who need to get out of that state, even though the route itself
 * needs no authentication and the body tokens are what get blacklisted/refreshed.
 */
public class PublicAuthRouteBearerTokenResolver implements BearerTokenResolver {

    private final BearerTokenResolver delegate = new DefaultBearerTokenResolver();

    @Override
    public String resolve(@NonNull HttpServletRequest request) {
        if (isAuthRoute(request)) {
            return null;
        }
        return delegate.resolve(request);
    }

    private boolean isAuthRoute(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path.equals(ApiPaths.Auth.ROOT) || path.startsWith(ApiPaths.Auth.ROOT + "/");
    }
}
