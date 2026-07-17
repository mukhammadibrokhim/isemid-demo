package uz.uzinfocom.app.platform.iam.application.shared.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.iam.application.shared.cache.OrganizationCacheConfig;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationLocalizedName;
import uz.uzinfocom.app.platform.iam.application.shared.exception.OrganizationResolutionException;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheManager = "securityCacheManager")
public class OrganizationIdResolver {

    private final OrganizationRepository organizationRepository;

    @Cacheable(
            cacheNames = OrganizationCacheConfig.ORGANIZATION_ID_BY_UUID,
            key = "#uuid",
            cacheManager = "securityCacheManager",
            sync = true
    )
    public Long resolveActiveId(UUID uuid) {
        if (uuid == null) {
            throw new OrganizationResolutionException("validation.organization.required");
        }
        return organizationRepository.findActiveIdByUuid(uuid)
                .orElseThrow(() -> new OrganizationResolutionException("error.organization.not-found", uuid));
    }

    /**
     * Returns the raw, locale-independent name fields (never a resolved
     * display string) so the cache holds a single entry per organization id
     * regardless of how many locales end up requesting it. Callers pick the
     * right value for the current locale via LocalizedTextResolver.
     */
    @Cacheable(
            cacheNames = OrganizationCacheConfig.ORGANIZATION_NAME_BY_ID,
            key = "#id",
            cacheManager = "securityCacheManager",
            sync = true
    )
    public OrganizationLocalizedName resolveActiveNameFields(Long id) {
        if (id == null) {
            throw new OrganizationResolutionException("validation.organization.required");
        }
        return organizationRepository.findActiveNameFieldsById(id)
                .orElseThrow(() -> new OrganizationResolutionException("error.organization.not-found", id));
    }
}
