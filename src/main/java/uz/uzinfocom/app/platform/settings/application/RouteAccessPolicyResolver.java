package uz.uzinfocom.app.platform.settings.application;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import uz.uzinfocom.app.platform.security.route.RequestPolicy;
import uz.uzinfocom.app.platform.settings.config.SettingsCacheConfig;
import uz.uzinfocom.app.platform.settings.domain.RouteAccessPolicy;
import uz.uzinfocom.app.platform.settings.repository.RouteAccessPolicyRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves a request path to a {@link RequestPolicy} using the DB-backed
 * {@link RouteAccessPolicy} table instead of the (deleted)
 * {@code SecurityRouteCatalog} static lists. Consumed by both
 * {@code DynamicRouteAuthorizationManager} (the raw Spring Security gate,
 * which can no longer bake a fixed pattern list into the filter chain at
 * startup) and {@code RequestPolicyResolver} (the org-header/role-validation
 * policy) - one cached lookup, one source of truth for both layers.
 *
 * <p>The policy list itself is cached whole ({@link SettingsCacheConfig#ROUTE_ACCESS_POLICIES},
 * short TTL) since it's small and read on every request; write paths (see
 * {@code RouteAccessPolicyCommandService}) evict it immediately on any change.
 */
@Component
public class RouteAccessPolicyResolver {

    private static final String CACHE_KEY = "ALL";

    private final RouteAccessPolicyRepository routeAccessPolicyRepository;
    private final Cache cache;
    private final PathPatternParser parser = new PathPatternParser();
    private final Map<String, PathPattern> patternCache = new ConcurrentHashMap<>();

    public RouteAccessPolicyResolver(
            RouteAccessPolicyRepository routeAccessPolicyRepository,
            @Qualifier("securityCacheManager") CacheManager cacheManager
    ) {
        this.routeAccessPolicyRepository = routeAccessPolicyRepository;
        this.cache = cacheManager.getCache(SettingsCacheConfig.ROUTE_ACCESS_POLICIES);
    }

    public RequestPolicy resolve(String path) {
        for (RouteAccessPolicy policy : policies()) {
            if (matches(policy.getPattern(), path)) {
                return new RequestPolicy(
                        policy.isOpen(),
                        policy.isOrganizationHeaderRequired(),
                        policy.isRoleValidationRequired(),
                        policy.getPattern()
                );
            }
        }
        return RequestPolicy.defaultProtectedRoute();
    }

    public void evictAll() {
        cache.evict(CACHE_KEY);
    }

    @SuppressWarnings("unchecked")
    private List<RouteAccessPolicy> policies() {
        Cache.ValueWrapper cached = cache.get(CACHE_KEY);
        if (cached != null) {
            return (List<RouteAccessPolicy>) cached.get();
        }

        List<RouteAccessPolicy> policies = routeAccessPolicyRepository.findByEnabledTrueOrderByDisplayOrderAsc();
        cache.put(CACHE_KEY, policies);
        return policies;
    }

    private boolean matches(String pattern, String path) {
        PathPattern compiled = patternCache.computeIfAbsent(pattern, parser::parse);
        return compiled.matches(PathContainer.parsePath(path));
    }
}
