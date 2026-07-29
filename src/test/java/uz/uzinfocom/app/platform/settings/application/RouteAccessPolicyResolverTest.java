package uz.uzinfocom.app.platform.settings.application;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import uz.uzinfocom.app.platform.security.route.RequestPolicy;
import uz.uzinfocom.app.platform.settings.config.SettingsCacheConfig;
import uz.uzinfocom.app.platform.settings.domain.RouteAccessPolicy;
import uz.uzinfocom.app.platform.settings.repository.RouteAccessPolicyRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RouteAccessPolicyResolverTest {

    private final RouteAccessPolicyRepository routeAccessPolicyRepository = mock(RouteAccessPolicyRepository.class);
    private final Cache cache = new ConcurrentMapCache(SettingsCacheConfig.ROUTE_ACCESS_POLICIES);
    private final CacheManager cacheManager = mock(CacheManager.class);

    private final RouteAccessPolicyResolver resolver;

    {
        when(cacheManager.getCache(SettingsCacheConfig.ROUTE_ACCESS_POLICIES)).thenReturn(cache);
        resolver = new RouteAccessPolicyResolver(routeAccessPolicyRepository, cacheManager);
    }

    private RouteAccessPolicy policy(String pattern, int order, boolean open, boolean orgHeader, boolean role) {
        return RouteAccessPolicy.builder()
                .pattern(pattern)
                .displayOrder(order)
                .open(open)
                .organizationHeaderRequired(orgHeader)
                .roleValidationRequired(role)
                .enabled(true)
                .build();
    }

    @Test
    void aPathMatchingAnOpenPatternResolvesToAPublicRoute() {
        when(routeAccessPolicyRepository.findByEnabledTrueOrderByDisplayOrderAsc()).thenReturn(
                List.of(policy("/v1/auth/**", 1, true, false, false))
        );

        RequestPolicy result = resolver.resolve("/v1/auth/login/sso-web");

        assertThat(result.publicRoute()).isTrue();
        assertThat(result.organizationHeaderRequired()).isFalse();
        assertThat(result.roleValidationRequired()).isFalse();
    }

    @Test
    void firstMatchingRowWinsWhenMultiplePatternsCouldMatch() {
        when(routeAccessPolicyRepository.findByEnabledTrueOrderByDisplayOrderAsc()).thenReturn(
                List.of(
                        policy("/v1/admin/settings", 1, false, false, false),
                        policy("/v1/admin/**", 2, false, false, true)
                )
        );

        RequestPolicy exact = resolver.resolve("/v1/admin/settings");
        assertThat(exact.roleValidationRequired()).isFalse();

        RequestPolicy fallthrough = resolver.resolve("/v1/admin/other");
        assertThat(fallthrough.roleValidationRequired()).isTrue();
    }

    @Test
    void aPathMatchingNoRowFallsBackToTheDefaultProtectedRoute() {
        when(routeAccessPolicyRepository.findByEnabledTrueOrderByDisplayOrderAsc()).thenReturn(List.of());

        RequestPolicy result = resolver.resolve("/v1/anything/not/configured");

        assertThat(result).isEqualTo(RequestPolicy.defaultProtectedRoute());
    }

    @Test
    void thePolicyListIsCachedAcrossCalls() {
        when(routeAccessPolicyRepository.findByEnabledTrueOrderByDisplayOrderAsc()).thenReturn(List.of());

        resolver.resolve("/v1/a");
        resolver.resolve("/v1/b");

        verify(routeAccessPolicyRepository).findByEnabledTrueOrderByDisplayOrderAsc();
    }
}
