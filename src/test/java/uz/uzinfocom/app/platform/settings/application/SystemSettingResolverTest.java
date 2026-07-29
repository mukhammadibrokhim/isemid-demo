package uz.uzinfocom.app.platform.settings.application;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import uz.uzinfocom.app.platform.settings.config.SettingsCacheConfig;
import uz.uzinfocom.app.platform.settings.domain.SystemSetting;
import uz.uzinfocom.app.platform.settings.domain.SystemSettingValueType;
import uz.uzinfocom.app.platform.settings.repository.SystemSettingRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemSettingResolverTest {

    private final SystemSettingRepository systemSettingRepository = mock(SystemSettingRepository.class);
    private final Cache cache = new ConcurrentMapCache(SettingsCacheConfig.SYSTEM_SETTING_BY_KEY);
    private final CacheManager cacheManager = mock(CacheManager.class);

    private final SystemSettingResolver resolver;

    {
        when(cacheManager.getCache(SettingsCacheConfig.SYSTEM_SETTING_BY_KEY)).thenReturn(cache);
        resolver = new SystemSettingResolver(systemSettingRepository, cacheManager);
    }

    private SystemSetting activeSetting(String key, String value) {
        SystemSetting setting = new SystemSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setValueType(SystemSettingValueType.STRING);
        setting.setActive(true);
        return setting;
    }

    @Test
    void fallsBackToTheDefaultWhenNoSettingRowExists() {
        when(systemSettingRepository.findBySettingKeyAndDeletedFalse("missing.key")).thenReturn(Optional.empty());

        assertThat(resolver.resolveBoolean("missing.key", true)).isTrue();
        assertThat(resolver.resolveString("missing.key", "fallback")).isEqualTo("fallback");
        assertThat(resolver.resolveLong("missing.key", 42L)).isEqualTo(42L);
    }

    @Test
    void fallsBackToTheDefaultWhenTheSettingIsInactive() {
        SystemSetting inactive = activeSetting("feature.flag", "true");
        inactive.setActive(false);
        when(systemSettingRepository.findBySettingKeyAndDeletedFalse("feature.flag")).thenReturn(Optional.of(inactive));

        assertThat(resolver.resolveBoolean("feature.flag", false)).isFalse();
    }

    @Test
    void returnsTheDbValueWhenAnActiveSettingExists() {
        when(systemSettingRepository.findBySettingKeyAndDeletedFalse("feature.flag"))
                .thenReturn(Optional.of(activeSetting("feature.flag", "true")));

        assertThat(resolver.resolveBoolean("feature.flag", false)).isTrue();
    }

    @Test
    void parsesACommaSeparatedListAndTrimsEntries() {
        when(systemSettingRepository.findBySettingKeyAndDeletedFalse("form058.allowed-sources"))
                .thenReturn(Optional.of(activeSetting("form058.allowed-sources", "QR, MANUAL,DMED")));

        assertThat(resolver.resolveStringList("form058.allowed-sources", List.of("DEFAULT")))
                .containsExactly("QR", "MANUAL", "DMED");
    }

    @Test
    void cachesTheLookupSoARepeatedCallDoesNotHitTheRepositoryAgain() {
        when(systemSettingRepository.findBySettingKeyAndDeletedFalse("cached.key")).thenReturn(Optional.empty());

        resolver.resolveString("cached.key", "a");
        resolver.resolveString("cached.key", "a");

        verify(systemSettingRepository).findBySettingKeyAndDeletedFalse("cached.key");
    }

    @Test
    void evictionForcesTheNextCallToHitTheRepositoryAgain() {
        when(systemSettingRepository.findBySettingKeyAndDeletedFalse("evicted.key")).thenReturn(Optional.empty());

        resolver.resolveString("evicted.key", "a");
        resolver.evict("evicted.key");
        resolver.resolveString("evicted.key", "a");

        verify(systemSettingRepository, org.mockito.Mockito.times(2)).findBySettingKeyAndDeletedFalse("evicted.key");
    }
}
