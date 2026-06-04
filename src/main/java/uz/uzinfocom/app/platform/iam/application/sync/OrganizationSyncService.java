package uz.uzinfocom.app.platform.iam.application.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.application.sync.mapper.OrganizationRemoteMapper;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.ProviderIamRemoteClient;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationSyncService {

    private final OrganizationRepository organizationRepository;
    private final ProviderIamRemoteClient remoteClient;
    private final OrganizationRemoteMapper mapper;

    @Transactional
    @Cacheable(
            cacheManager = "securityCacheManager",
            cacheNames = SecurityCacheNames.ORGANIZATION_SYNC_BY_PROVIDER_AND_UUID,
            key = "#providerKey + ':' + #organizationUuid"
    )
    public Organization resolve(String providerKey, UUID organizationUuid, String rawToken) {
        return organizationRepository.findByUuid(organizationUuid)
                .orElseGet(() -> provision(providerKey, organizationUuid, rawToken));
    }

    private Organization provision(String providerKey, UUID organizationUuid, String rawToken) {
        Organization entity = mapper.toEntity(remoteClient.fetchOrganization(providerKey, organizationUuid, rawToken));
        try {
            return organizationRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException concurrentInsert) {
            return organizationRepository.findByUuid(organizationUuid)
                    .orElseThrow(() -> concurrentInsert);
        }
    }
}
