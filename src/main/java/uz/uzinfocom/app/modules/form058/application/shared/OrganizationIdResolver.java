package uz.uzinfocom.app.modules.form058.application.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ValidationException;
import uz.uzinfocom.app.platform.iam.application.shared.cache.OrganizationCacheConfig;
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
            throw new Form058ValidationException("validation.form058.organization.required");
        }
        return organizationRepository.findActiveIdByUuid(uuid)
                .orElseThrow(() -> new Form058ValidationException("error.organization.not-found", uuid));
    }
}
