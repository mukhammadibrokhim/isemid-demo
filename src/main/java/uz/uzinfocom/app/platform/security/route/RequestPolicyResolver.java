package uz.uzinfocom.app.platform.security.route;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import uz.uzinfocom.app.platform.security.whitelist.SecurityRouteCatalog;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RequestPolicyResolver {

    private final PathPatternParser parser = new PathPatternParser();
    private final Map<String, PathPattern> patternCache = new ConcurrentHashMap<>();

    public RequestPolicy resolve(HttpServletRequest request) {
        String requestPath = normalize(request);

        Optional<Map.Entry<String, SecurityRouteCatalog.RoutePolicyRule>> explicitRule =
                SecurityRouteCatalog.POLICY_RULES.entrySet().stream()
                        .filter(entry -> matches(entry.getKey(), requestPath))
                        .findFirst();

        if (explicitRule.isPresent()) {
            Map.Entry<String, SecurityRouteCatalog.RoutePolicyRule> match = explicitRule.get();
            SecurityRouteCatalog.RoutePolicyRule rule = match.getValue();
            boolean publicRoute = SecurityRouteCatalog.OPEN_PATTERNS.stream()
                    .anyMatch(pattern -> matches(pattern, requestPath));
            return new RequestPolicy(
                    publicRoute,
                    rule.organizationHeaderRequired(),
                    rule.roleValidationRequired(),
                    match.getKey()
            );
        }

        Optional<String> openPattern = SecurityRouteCatalog.OPEN_PATTERNS.stream()
                .filter(pattern -> matches(pattern, requestPath))
                .findFirst();

        if (openPattern.isPresent()) {
            return RequestPolicy.publicRoute(openPattern.get());
        }

        Optional<String> noOrgPattern = SecurityRouteCatalog.NO_ORG_HEADER_PATTERNS.stream()
                .filter(pattern -> matches(pattern, requestPath))
                .findFirst();

        return noOrgPattern.map(s -> new RequestPolicy(false, false, true, s)).orElseGet(RequestPolicy::defaultProtectedRoute);

    }

    private boolean matches(String pattern, String requestPath) {
        PathPattern compiled = patternCache.computeIfAbsent(pattern, parser::parse);
        return compiled.matches(PathContainer.parsePath(requestPath));
    }

    private String normalize(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return StringUtils.hasText(requestUri) ? requestUri : "/";
    }
}
