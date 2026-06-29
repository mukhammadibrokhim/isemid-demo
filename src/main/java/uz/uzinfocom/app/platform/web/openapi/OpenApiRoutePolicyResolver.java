package uz.uzinfocom.app.platform.web.openapi;

import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import uz.uzinfocom.app.platform.security.whitelist.SecurityRouteCatalog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OpenApiRoutePolicyResolver {

    private final PathPatternParser pathPatternParser = new PathPatternParser();
    private final Map<String, PathPattern> pathPatternCache = new ConcurrentHashMap<>();

    public boolean isPublicRoute(String path) {
        return SecurityRouteCatalog.OPEN_PATTERNS.stream()
                .anyMatch(pattern -> matches(pattern, path));
    }

    public boolean isOrganizationHeaderRequired(String path) {
        return SecurityRouteCatalog.POLICY_RULES.entrySet()
                .stream()
                .filter(entry -> matches(entry.getKey(), path))
                .findFirst()
                .map(entry -> entry.getValue().organizationHeaderRequired())
                .orElseGet(() -> SecurityRouteCatalog.NO_ORG_HEADER_PATTERNS.stream()
                        .noneMatch(pattern -> matches(pattern, path)));
    }

    private boolean matches(String pattern, String path) {
        PathPattern compiledPattern = pathPatternCache.computeIfAbsent(
                pattern,
                pathPatternParser::parse
        );

        return compiledPattern.matches(PathContainer.parsePath(path));
    }
}