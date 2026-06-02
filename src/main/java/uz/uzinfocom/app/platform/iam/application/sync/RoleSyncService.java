package uz.uzinfocom.app.platform.iam.application.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.repository.RoleRepository;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

    @Transactional(readOnly = true)
    public Set<Role> resolve(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Set.of();
        }

        LinkedHashSet<Role> roles = new LinkedHashSet<>();

        roleNames.stream()
                .map(this::normalizeRoleName)
                .filter(Objects::nonNull)
                .filter(this::isIsemidRole)
                .map(this::resolveRoleIdCached)
                .flatMap(Optional::stream)
                .map(roleRepository::findWithPermissionsById)
                .flatMap(Optional::stream)
                .filter(Role::isAvailableForAuthorization)
                .forEach(roles::add);

        return Collections.unmodifiableSet(roles);
    }

    @Transactional(readOnly = true)
    public Optional<Role> resolveOne(String roleName) {
        String normalizedRoleName = normalizeRoleName(roleName);

        if (!isIsemidRole(normalizedRoleName)) {
            return Optional.empty();
        }

        return resolveRoleIdCached(normalizedRoleName)
                .flatMap(roleRepository::findWithPermissionsById)
                .filter(Role::isAvailableForAuthorization);
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

    private Optional<Long> resolveRoleIdCached(String normalizedRoleName) {
        Cache cache = requireCache();

        Optional<Long> cachedRoleId = cache.get(normalizedRoleName, Optional.class);

        if (cachedRoleId != null) {
            return cachedRoleId;
        }

        Optional<Long> roleId = resolveLocalRoleId(normalizedRoleName);

        /*
         * Do not cache Optional.empty().
         * A role may be created or activated later, and caching an empty result
         * would keep authorization resolution stale until the cache expires.
         */
        roleId.ifPresent(id -> cache.put(normalizedRoleName, roleId));

        return roleId;
    }

    private Optional<Long> resolveLocalRoleId(String normalizedRoleName) {
        Optional<Long> existingRoleId = roleRepository.findIdByNormalizedName(normalizedRoleName);

        if (existingRoleId.isPresent()) {
            return existingRoleId;
        }

        if (!properties.isCreateMissingRoles()) {
            log.debug("Role not found and auto-create is disabled. roleName={}", normalizedRoleName);
            return Optional.empty();
        }

        return createInactivePlaceholderSafely(normalizedRoleName)
                .map(Role::getId);
    }

    private Optional<Role> createInactivePlaceholderSafely(String normalizedRoleName) {
        try {
            Role placeholder = Role.builder()
                    .name(normalizedRoleName)
                    .active(false)
                    /*
                     * Do not set deleted=false here.
                     * The database does not have a deleted column.
                     * Soft delete is handled through the deleted_at column.
                     */
                    .build();

            Role savedRole = roleRepository.saveAndFlush(placeholder);

            log.info("Inactive placeholder role created. roleId={}, roleName={}",
                    savedRole.getId(), normalizedRoleName);

            return Optional.of(savedRole);

        } catch (DataIntegrityViolationException ex) {
            /*
             * Multiple concurrent login requests may try to create the same role.
             * If a unique constraint violation occurs, reload the role from the database.
             */
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