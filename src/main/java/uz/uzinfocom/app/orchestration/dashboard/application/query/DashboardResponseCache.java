package uz.uzinfocom.app.orchestration.dashboard.application.query;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.function.Supplier;

/**
 * Short-lived (30s) cache in front of the dashboard query services. Dashboard
 * numbers are scoped by the caller's current organization and carry
 * locale-resolved names, so the key is module + organization id + locale tag.
 * Exceptions are never cached. Without a current organization the loader runs
 * uncached so it raises its usual scope violation.
 */
@Component
public class DashboardResponseCache {

    private final CacheManager cacheManager;

    public DashboardResponseCache(@Qualifier("securityCacheManager") CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String module, Supplier<T> loader) {
        Long organizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElse(null);
        Cache cache = cacheManager.getCache(SecurityCacheNames.DASHBOARD_RESPONSE);

        if (organizationId == null || cache == null) {
            return loader.get();
        }

        String key = module + ':' + organizationId + ':' + LocaleContextHolder.getLocale().toLanguageTag();
        // Cache#get(key, loader) is atomic per key: concurrent identical requests
        // share one computation instead of each fanning out its own parallel queries.
        return (T) cache.get(key, loader::get);
    }
}
