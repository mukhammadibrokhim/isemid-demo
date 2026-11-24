package uz.uzinfocom.app.platform.devpanel.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.devpanel.repository.DevUserRepository;

import java.util.Optional;

/**
 * Per-request account lookup for {@code DevTokenAuthenticationFilter}. Cached
 * (short TTL, evicted on every account/credential change) so a token costs no
 * DB round-trip per request - the original perf problem of this panel.
 * Optional is not cached by Spring when empty only with {@code unless}, so an
 * unknown id is not remembered.
 */
@Service
@RequiredArgsConstructor
public class DevPrincipalLoader {

    private final DevUserRepository devUserRepository;

    @Transactional(readOnly = true)
    @Cacheable(
            cacheManager = "securityCacheManager",
            cacheNames = SecurityCacheNames.DEV_PANEL_PRINCIPAL,
            key = "#devUserId",
            unless = "#result == null"
    )
    public DevUserPrincipal load(Long devUserId) {
        Optional<DevUserPrincipal> principal = devUserRepository.findById(devUserId).map(DevUserPrincipal::new);
        return principal.orElse(null);
    }
}
