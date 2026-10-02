package uz.uzinfocom.app.modules.iam.application.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.modules.iam.application.sync.mapper.OrganizationRemoteMapper;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.infrastructure.remote.ProviderIamRemoteClient;
import uz.uzinfocom.app.modules.iam.infrastructure.remote.payload.RemoteOrganizationPayload;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class OrganizationSyncService {

    private final OrganizationRepository organizationRepository;
    private final ProviderIamRemoteClient remoteClient;
    private final OrganizationRemoteMapper mapper;
    private final CacheManager securityCacheManager;

    public OrganizationSyncService(
            OrganizationRepository organizationRepository,
            ProviderIamRemoteClient remoteClient,
            OrganizationRemoteMapper mapper,
            @Qualifier("securityCacheManager") CacheManager securityCacheManager
    ) {
        this.organizationRepository = organizationRepository;
        this.remoteClient = remoteClient;
        this.mapper = mapper;
        this.securityCacheManager = securityCacheManager;
    }

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
        Organization entity = mapper.toEntity(payload, providerKey);
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

    /**
     * Admin-triggered refresh for one organization by its own uuid: fetches
     * the latest data from the owning IAM provider (api2.ssv.uz for "sso",
     * fhir.dhp.uz for "dhp") and writes it onto the existing row, or
     * provisions the row if it doesn't exist locally yet. Unlike
     * {@link #resolve}, this always calls out to the remote system and is
     * not cached.
     *
     * <p>The provider to query is resolved in order: an explicit
     * {@code providerKeyOverride} (caller pins it), else the organization's
     * own already-stored {@code providerKey} (kept stable across re-syncs so
     * an admin authenticated on a different provider can't accidentally
     * switch it), else {@code callerProviderKeyOrNull} — the provider that
     * issued the calling admin's own token, used as a convenience default for
     * organizations never synced before.
     */
    @Transactional
    public Organization syncByUuid(
            UUID organizationUuid,
            String providerKeyOverride,
            String callerProviderKeyOrNull,
            String rawToken
    ) {
        Optional<Organization> existing = organizationRepository.findByUuid(organizationUuid);
        String providerKey = resolveProviderKey(providerKeyOverride, existing, callerProviderKeyOrNull);

        RemoteOrganizationPayload payload = remoteClient.fetchOrganization(providerKey, organizationUuid, rawToken);

        Organization entity;
        if (existing.isPresent()) {
            entity = existing.get();
            mapper.updateEntity(entity, payload, providerKey);
        } else {
            entity = mapper.toEntity(payload, providerKey);
        }
        entity.setParent(resolveParent(providerKey, payload, rawToken));

        Organization saved = organizationRepository.saveAndFlush(entity);
        evictResolveCache(providerKey, organizationUuid);
        return saved;
    }

    private String resolveProviderKey(
            String providerKeyOverride,
            Optional<Organization> existing,
            String callerProviderKeyOrNull
    ) {
        if (StringUtils.hasText(providerKeyOverride)) {
            return providerKeyOverride;
        }

        Optional<String> storedProviderKey = existing
                .map(Organization::getProviderKey)
                .filter(StringUtils::hasText);
        if (storedProviderKey.isPresent()) {
            return storedProviderKey.get();
        }

        if (StringUtils.hasText(callerProviderKeyOrNull)) {
            return callerProviderKeyOrNull;
        }

        throw new IllegalArgumentException(
                "providerKey must be specified: organization has no known IAM provider on record yet");
    }

    private void evictResolveCache(String providerKey, UUID organizationUuid) {
        Cache cache = securityCacheManager.getCache(SecurityCacheNames.ORGANIZATION_SYNC_BY_PROVIDER_AND_UUID);
        if (cache != null) {
            cache.evict(providerKey + ":" + organizationUuid);
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
