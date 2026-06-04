package uz.uzinfocom.app.platform.iam.application.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.application.sync.dto.RoleSyncSnapshot;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.repository.RoleRepository;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleSyncService {

    private static final String APP_ROLE_PREFIX = "isemid_";

    private final RoleRepository roleRepository;
    private final RoleSyncProperties properties;
    private final CacheManager securityCacheManager;

    public RoleSyncService(
            RoleRepository roleRepository,
            RoleSyncProperties properties,
            @Qualifier("securityCacheManager") CacheManager securityCacheManager
    ) {
        this.roleRepository = roleRepository;
        this.properties = properties;
        this.securityCacheManager = securityCacheManager;
    }

    @Transactional
    public Set<Role> resolve(Set<String> roleNames) {
        Set<String> appRoleNames = normalizeAndValidateApplicationRoles(roleNames);

        LinkedHashSet<Role> roles = appRoleNames.stream()
                .map(this::resolveRoleSnapshotCached)
                .flatMap(Optional::stream)
                .filter(RoleSyncSnapshot::availableForAuthorization)
                .map(RoleSyncSnapshot::id)
                .map(roleRepository::getReferenceById)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (roles.isEmpty()) {
            log.warn("Access denied: token contains ISEMID roles, but no active local role was resolved. tokenRoles={}",
                    appRoleNames);

            throw new AccessDeniedException(
                    "Access denied: no active ISEMID role is assigned to this user."
            );
        }

        return Collections.unmodifiableSet(roles);
    }

    @Transactional
    public Optional<Role> resolveOne(String roleName) {
        String normalizedRoleName = normalizeRoleName(roleName);

        if (!isIsemidRole(normalizedRoleName)) {
            throw new AccessDeniedException(
                    "Access denied: token does not contain required ISEMID role."
            );
        }

        return resolveRoleSnapshotCached(normalizedRoleName)
                .filter(RoleSyncSnapshot::availableForAuthorization)
                .map(RoleSyncSnapshot::id)
                .map(roleRepository::getReferenceById);
    }

    private Set<String> normalizeAndValidateApplicationRoles(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            log.warn("Access denied: token roles are empty.");
            throw new AccessDeniedException(
                    "Access denied: token does not contain required ISEMID role."
            );
        }

        Set<String> appRoleNames = roleNames.stream()
                .map(this::normalizeRoleName)
                .filter(Objects::nonNull)
                .filter(this::isIsemidRole)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (appRoleNames.isEmpty()) {
            log.warn("Access denied: token does not contain any role with required prefix. requiredPrefix={}, tokenRoles={}",
                    APP_ROLE_PREFIX, roleNames);

            throw new AccessDeniedException(
                    "Access denied: token does not contain required ISEMID role."
            );
        }

        return Collections.unmodifiableSet(appRoleNames);
    }

    private String normalizeRoleName(String roleName) {
        if (roleName == null) {
            return null;
        }

        String normalized = roleName.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private boolean isIsemidRole(String normalizedRoleName) {
        return normalizedRoleName != null
                && normalizedRoleName.startsWith(APP_ROLE_PREFIX);
    }

    private Optional<RoleSyncSnapshot> resolveRoleSnapshotCached(String normalizedRoleName) {
        Cache cache = requireCache();

        RoleSyncSnapshot cachedRole = cache.get(normalizedRoleName, RoleSyncSnapshot.class);

        if (cachedRole != null) {
            return Optional.of(cachedRole);
        }

        Optional<RoleSyncSnapshot> role = resolveLocalRoleSnapshot(normalizedRoleName);

        // Do not cache empty result. Role may be created or activated later.
        role.ifPresent(snapshot -> cache.put(normalizedRoleName, snapshot));

        return role;
    }

    private Optional<RoleSyncSnapshot> resolveLocalRoleSnapshot(String normalizedRoleName) {
        Optional<Role> existingRole = roleRepository.findByNormalizedName(normalizedRoleName);

        if (existingRole.isPresent()) {
            return existingRole.map(RoleSyncSnapshot::from);
        }

        if (!properties.isCreateMissingRoles()) {
            log.debug("Role not found and auto-create is disabled. roleName={}", normalizedRoleName);
            return Optional.empty();
        }

        return createInactivePlaceholderSafely(normalizedRoleName)
                .map(RoleSyncSnapshot::from);
    }

    private Optional<Role> createInactivePlaceholderSafely(String normalizedRoleName) {
        try {
            Role placeholder = Role.builder()
                    .name(normalizedRoleName)
                    .active(false)
                    .build();

            Role savedRole = roleRepository.saveAndFlush(placeholder);

            log.info("Inactive placeholder role created. roleId={}, roleName={}",
                    savedRole.getId(), normalizedRoleName);

            return Optional.of(savedRole);

        } catch (DataIntegrityViolationException ex) {
            log.warn("Role was created concurrently. Trying to reload. roleName={}", normalizedRoleName);

            return roleRepository.findByNormalizedName(normalizedRoleName);
        }
    }

    private Cache requireCache() {
        Cache cache = securityCacheManager.getCache(SecurityCacheNames.IAM_ROLE_BY_NAME);

        if (cache == null) {
            throw new IllegalStateException(
                    "Cache is not configured: " + SecurityCacheNames.IAM_ROLE_BY_NAME
            );
        }

        return cache;
    }
}
