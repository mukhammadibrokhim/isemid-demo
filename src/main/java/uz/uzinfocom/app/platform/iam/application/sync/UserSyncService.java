package uz.uzinfocom.app.platform.iam.application.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.application.sync.dto.UserSyncResult;
import uz.uzinfocom.app.platform.iam.application.sync.mapper.UserRemoteMapper;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.ProviderIamRemoteClient;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.security.claims.ExternalIdentityPayload;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class UserSyncService {

    private final UserRepository userRepository;
    private final ProviderIamRemoteClient remoteClient;
    private final UserRemoteMapper mapper;
    private final CacheManager securityCacheManager;

    public UserSyncService(
            UserRepository userRepository,
            ProviderIamRemoteClient remoteClient,
            UserRemoteMapper mapper,
            @Qualifier("securityCacheManager") CacheManager securityCacheManager
    ) {
        this.userRepository = userRepository;
        this.remoteClient = remoteClient;
        this.mapper = mapper;
        this.securityCacheManager = securityCacheManager;
    }

    @Transactional
    public UserSyncResult resolve(
            ExternalIdentityPayload payload,
            String rawToken,
            List<Organization> organizations,
            Set<Role> roles
    ) {
        return userRepository.findByUuid(payload.practitionerUuid())
                .map(existing -> syncExisting(payload, existing, organizations, roles))
                .orElseGet(() -> provision(payload, rawToken, organizations, roles));
    }

    private UserSyncResult syncExisting(
            ExternalIdentityPayload payload,
            User user,
            List<Organization> organizations,
            Set<Role> roles
    ) {
        boolean userDataChanged = applyPayloadUserFields(user, payload);

        Set<UUID> currentOrganizationUuids = organizationUuids(user.getOrganizations());
        Set<UUID> tokenOrganizationUuids = organizationUuids(organizations);
        boolean organizationsChanged = !currentOrganizationUuids.equals(tokenOrganizationUuids);

        if (organizationsChanged) {
            user.setOrganizations(new LinkedHashSet<>(organizations));
        }

        Set<Long> currentRoleIds = roleIds(user.getRoles());
        Set<Long> tokenRoleIds = roleIds(roles);
        boolean rolesChanged = !currentRoleIds.equals(tokenRoleIds);

        if (rolesChanged) {
            user.setRoles(new LinkedHashSet<>(roles));
        }

        if (!userDataChanged && !organizationsChanged && !rolesChanged) {
            return UserSyncResult.unchanged(user);
        }

        User savedUser = userRepository.save(user);
        Set<UUID> affectedOrganizationUuids = union(currentOrganizationUuids, tokenOrganizationUuids);

        evictChangedSecurityCaches(
                savedUser.getId(),
                userDataChanged,
                organizationsChanged,
                rolesChanged,
                affectedOrganizationUuids
        );

        log.debug(
                "IAM user sync changed persisted data. userId={}, userDataChanged={}, organizationsChanged={}, rolesChanged={}",
                savedUser.getId(),
                userDataChanged,
                organizationsChanged,
                rolesChanged
        );

        return new UserSyncResult(
                savedUser,
                false,
                userDataChanged,
                organizationsChanged,
                rolesChanged,
                affectedOrganizationUuids
        );
    }

    private UserSyncResult provision(
            ExternalIdentityPayload payload,
            String rawToken,
            List<Organization> organizations,
            Set<Role> roles
    ) {
        User remoteUser = mapper.toEntity(
                remoteClient.fetchPractitioner(payload.providerKey(), payload.practitionerUuid(), rawToken),
                new LinkedHashSet<>(organizations),
                roles
        );
        applyPayloadUserFields(remoteUser, payload);

        try {
            User savedUser = userRepository.saveAndFlush(remoteUser);
            log.debug("IAM user sync provisioned missing user. userId={}", savedUser.getId());
            return UserSyncResult.created(savedUser);
        } catch (DataIntegrityViolationException concurrentInsert) {
            log.warn("User was provisioned concurrently. Trying to reload. practitionerUuid={}",
                    payload.practitionerUuid());

            return userRepository.findByUuid(payload.practitionerUuid())
                    .map(UserSyncResult::unchanged)
                    .orElseThrow(() -> concurrentInsert);
        }
    }

    private boolean applyPayloadUserFields(User user, ExternalIdentityPayload payload) {
        boolean changed = false;

        String username = trimToNull(payload.username());
        if (username != null && !Objects.equals(user.getUsername(), username)) {
            user.setUsername(username);
            changed = true;
        }

        String nnuzb = trimToNull(payload.nnuzb());
        if (nnuzb != null && !Objects.equals(user.getNnuzb(), nnuzb)) {
            user.setNnuzb(nnuzb);
            changed = true;
        }

        return changed;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Set<UUID> organizationUuids(Iterable<Organization> organizations) {
        if (organizations == null) {
            return Set.of();
        }

        LinkedHashSet<UUID> uuids = new LinkedHashSet<>();

        for (Organization organization : organizations) {
            if (organization != null && organization.getUuid() != null) {
                uuids.add(organization.getUuid());
            }
        }

        return Set.copyOf(uuids);
    }

    private Set<Long> roleIds(Iterable<Role> roles) {
        if (roles == null) {
            return Set.of();
        }

        LinkedHashSet<Long> ids = new LinkedHashSet<>();

        for (Role role : roles) {
            if (role != null && role.getId() != null) {
                ids.add(role.getId());
            }
        }

        return Set.copyOf(ids);
    }

    private Set<UUID> union(Set<UUID> left, Set<UUID> right) {
        LinkedHashSet<UUID> result = new LinkedHashSet<>();

        if (left != null) {
            result.addAll(left);
        }

        if (right != null) {
            result.addAll(right);
        }

        return Set.copyOf(result);
    }

    private void evictChangedSecurityCaches(
            Long userId,
            boolean userDataChanged,
            boolean organizationsChanged,
            boolean rolesChanged,
            Set<UUID> affectedOrganizationUuids
    ) {
        if (userId == null) {
            return;
        }

        if (userDataChanged || organizationsChanged) {
            evict(SecurityCacheNames.SECURITY_USER_BY_ID, userId);
        }

        if (rolesChanged) {
            evict(SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, userId);
        }

        if (organizationsChanged && affectedOrganizationUuids != null) {
            affectedOrganizationUuids.forEach(uuid ->
                    evict(
                            SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID,
                            selectedOrganizationCacheKey(userId, uuid)
                    )
            );
        }
    }

    private String selectedOrganizationCacheKey(Long userId, UUID organizationUuid) {
        return userId + ":" + organizationUuid;
    }

    private void evict(String cacheName, Object key) {
        Cache cache = securityCacheManager.getCache(cacheName);

        if (cache == null || key == null) {
            return;
        }

        cache.evict(key);
    }
}
