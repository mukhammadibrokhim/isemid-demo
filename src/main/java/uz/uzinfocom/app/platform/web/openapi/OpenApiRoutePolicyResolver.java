package uz.uzinfocom.app.platform.web.openapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import uz.uzinfocom.app.platform.settings.application.RouteAccessPolicyResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class OpenApiRoutePolicyResolver {

    private static final String DEV_PANEL_PATTERN = ApiPaths.Dev.ROOT + "/**";

    private final RouteAccessPolicyResolver routeAccessPolicyResolver;

    private final PathPatternParser pathPatternParser = new PathPatternParser();
    private final Map<String, PathPattern> pathPatternCache = new ConcurrentHashMap<>();

    public boolean isPublicRoute(String path) {
        return routeAccessPolicyResolver.resolve(path).publicRoute();
    }

    /**
     * {@code /v1/dev/**} authenticates via a separate local {@code DevUser}
     * HTTP Basic chain ({@code DevPanelSecurityConfig}), not the SSO/DHP
     * bearer JWT every other endpoint requires - documented with a distinct
     * security scheme rather than the misleading default bearerAuth.
     */
    public boolean isDevPanelRoute(String path) {
        return matches(DEV_PANEL_PATTERN, path);
    }

    public boolean isOrganizationHeaderRequired(String path) {
        return routeAccessPolicyResolver.resolve(path).organizationHeaderRequired();
    }

    private boolean matches(String pattern, String path) {
        PathPattern compiledPattern = pathPatternCache.computeIfAbsent(
                pattern,
                pathPatternParser::parse
        );

        return compiledPattern.matches(PathContainer.parsePath(path));
    }
}
