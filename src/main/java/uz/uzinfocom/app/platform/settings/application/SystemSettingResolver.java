package uz.uzinfocom.app.platform.settings.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.settings.config.SettingsCacheConfig;
import uz.uzinfocom.app.platform.settings.domain.SystemSetting;
import uz.uzinfocom.app.platform.settings.repository.SystemSettingRepository;

import java.util.Arrays;
import java.util.List;

/**
 * Read-through resolver for {@link SystemSetting} values: every method takes
 * the caller's current value (a {@code @ConfigurationProperties} field or a
 * hardcoded constant) as {@code defaultValue}, and only overrides it when an
 * {@code active}, non-deleted setting row exists for {@code key}. An empty or
 * misconfigured settings table is therefore never a behavior change - the app
 * runs exactly as it does today until someone deliberately adds a row.
 *
 * <p>Backed by a short-TTL cache ({@link SettingsCacheConfig#SYSTEM_SETTING_BY_KEY})
 * rather than hitting the DB on every call - write paths (see
 * {@code SystemSettingCommandService}) evict the affected key immediately, so
 * changes are visible right away rather than waiting out the TTL.
 */
@Slf4j
@Component
public class SystemSettingResolver {

    private final SystemSettingRepository systemSettingRepository;
    private final Cache cache;

    public SystemSettingResolver(
            SystemSettingRepository systemSettingRepository,
            @Qualifier("securityCacheManager") CacheManager cacheManager
    ) {
        this.systemSettingRepository = systemSettingRepository;
        this.cache = cacheManager.getCache(SettingsCacheConfig.SYSTEM_SETTING_BY_KEY);
    }

    public boolean resolveBoolean(String key, boolean defaultValue) {
        String raw = resolveRaw(key);
        return raw == null ? defaultValue : Boolean.parseBoolean(raw);
    }

    public String resolveString(String key, String defaultValue) {
        String raw = resolveRaw(key);
        return raw == null ? defaultValue : raw;
    }

    public long resolveLong(String key, long defaultValue) {
        String raw = resolveRaw(key);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException malformedValue) {
            log.warn("event=system_setting_parse_failure key={} expectedType=LONG value={}", key, raw);
            return defaultValue;
        }
    }

    public List<String> resolveStringList(String key, List<String> defaultValue) {
        String raw = resolveRaw(key);
        if (raw == null) {
            return defaultValue;
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    public void evict(String key) {
        cache.evict(key);
    }

    private String resolveRaw(String key) {
        Cache.ValueWrapper cached = cache.get(key);
        if (cached != null) {
            return (String) cached.get();
        }

        String value = systemSettingRepository.findBySettingKeyAndDeletedFalse(key)
                .filter(setting -> Boolean.TRUE.equals(setting.getActive()))
                .map(SystemSetting::getSettingValue)
                .orElse(null);

        cache.put(key, value);
        return value;
    }
}
