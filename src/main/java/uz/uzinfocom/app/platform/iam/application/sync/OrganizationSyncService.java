package uz.uzinfocom.app.platform.iam.application.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.application.sync.mapper.OrganizationRemoteMapper;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.ProviderIamRemoteClient;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.payload.RemoteOrganizationPayload;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;

import java.util.UUID;

@Slf4j
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
        RemoteOrganizationPayload payload = remoteClient.fetchOrganization(providerKey, organizationUuid, rawToken);
        Organization entity = mapper.toEntity(payload);
        entity.setParent(resolveParent(providerKey, payload, rawToken));

        try {
            return organizationRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException concurrentInsert) {
            log.warn("Organization was provisioned concurrently. Trying to reload. organizationUuid={}",
                    organizationUuid);

            return organizationRepository.findByUuid(organizationUuid)
                    .orElseThrow(() -> concurrentInsert);
        }
    }

    private Organization resolveParent(String providerKey, RemoteOrganizationPayload payload, String rawToken) {
        String parentUuidRaw = payload.parentOrganizationUuid();
        if (!StringUtils.hasText(parentUuidRaw)) {
            return null;
        }

        UUID parentUuid;
        try {
            parentUuid = UUID.fromString(parentUuidRaw);
        } catch (IllegalArgumentException invalidUuid) {
            log.warn("Organization {} has a non-UUID parent reference '{}'; skipping parent link",
                    payload.uuid(), parentUuidRaw);
            return null;
        }

        return resolve(providerKey, parentUuid, rawToken);
    }
}
