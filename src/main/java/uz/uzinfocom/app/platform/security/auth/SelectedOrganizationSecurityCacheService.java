package uz.uzinfocom.app.platform.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SelectedOrganizationSecurityCacheService {

    private final SecurityUserCacheService securityUserCacheService;

    @Cacheable(
            cacheManager = "securityCacheManager",
            cacheNames = uz.uzinfocom.app.platform.cache.SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID,
            key = "#userId + ':' + #selectedOrganizationUuid",
            condition = "#userId != null && #selectedOrganizationUuid != null"
    )
    public Optional<CachedSecurityOrganization> resolveSelectedOrganization(
            Long userId,
            UUID selectedOrganizationUuid
    ) {
        return securityUserCacheService.loadByUserId(userId)
                .flatMap(user -> user.findOrganizationByUuid(selectedOrganizationUuid));
    }
}
