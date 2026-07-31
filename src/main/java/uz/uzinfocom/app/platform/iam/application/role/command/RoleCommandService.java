package uz.uzinfocom.app.platform.iam.application.role.command;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.application.role.command.dto.RoleCreateRequest;
import uz.uzinfocom.app.platform.iam.application.role.command.dto.RolePermissionItemRequest;
import uz.uzinfocom.app.platform.iam.application.role.command.dto.RolePermissionUpdateRequest;
import uz.uzinfocom.app.platform.iam.application.role.command.dto.RoleUpdateRequest;
import uz.uzinfocom.app.platform.iam.application.role.query.RoleQueryService;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleDetailResponse;
import uz.uzinfocom.app.platform.iam.domain.Action;
import uz.uzinfocom.app.platform.iam.domain.Permission;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.RolePermission;
import uz.uzinfocom.app.platform.iam.repository.ActionRepository;
import uz.uzinfocom.app.platform.iam.repository.PermissionRepository;
import uz.uzinfocom.app.platform.iam.repository.RoleRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class RoleCommandService {

    private static final String MANAGE_ACTION_CODE = "MANAGE";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ActionRepository actionRepository;
    private final RoleQueryService roleQueryService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_BY_NAME, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS, allEntries = true)
    })
    public RoleDetailResponse create(RoleCreateRequest request) {
        String name = normalizeName(request.name());

        if (roleRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("role.name.already_exists", name);
        }

        Role role = Role.builder()
                .name(name)
                .active(request.active() == null || Boolean.TRUE.equals(request.active()))
                .deleted(false)
                .descriptionUz(request.descriptionUz())
                .descriptionRu(request.descriptionRu())
                .descriptionUzCyril(request.descriptionUzCyril())
                .descriptionKaa(request.descriptionKaa())
                .build();

        Role saved = roleRepository.saveAndFlush(role);
        return roleQueryService.findDetail(saved.getId());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_BY_NAME, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS, allEntries = true)
    })
    public RoleDetailResponse update(Long id, RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("role.not_found", id));

        if (Boolean.TRUE.equals(role.getDeleted())) {
            throw new ConflictException("role.update.deleted_conflict", id);
        }

        String name = normalizeName(request.name());

        if (roleRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("role.name.already_exists", name);
        }

        role.setName(name);
        role.setDescriptionUz(request.descriptionUz());
        role.setDescriptionRu(request.descriptionRu());
        role.setDescriptionUzCyril(request.descriptionUzCyril());
        role.setDescriptionKaa(request.descriptionKaa());
        role.setActive(request.active());

        roleRepository.saveAndFlush(role);
        return roleQueryService.findDetail(id);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_BY_NAME, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS, allEntries = true)
    })
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("role.not_found", id));

        if (Boolean.TRUE.equals(role.getDeleted())) {
            return;
        }

        role.setActive(false);
        role.setDeleted(true);
        role.setDeletedAt(LocalDateTime.now());

        roleRepository.save(role);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_BY_NAME, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS, allEntries = true)
    })
    public void restore(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("role.not_found", id));

        if (Boolean.FALSE.equals(role.getDeleted())) {
            return;
        }

        role.setDeleted(false);
        role.setDeletedAt(null);
        role.setActive(true);

        roleRepository.save(role);
    }

    /**
     * Adds/merges permissions into existing role permissions.
     * <p>
     * Example:
     * Existing: PATIENT -> READ
     * Request : PATIENT -> UPDATE
     * Result  : PATIENT -> READ, UPDATE
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS, allEntries = true)
    })
    public RoleDetailResponse addPermissions(Long roleId, RolePermissionUpdateRequest request) {
        Role role = getAvailableRole(roleId);

        Long manageActionId = resolveManageActionId();
        Map<Long, Set<Long>> requestedPermissions = normalizeRequest(request, manageActionId);

        for (Map.Entry<Long, Set<Long>> entry : requestedPermissions.entrySet()) {
            Long permissionId = entry.getKey();
            Set<Long> requestedActionIds = entry.getValue();

            Permission permission = getAvailablePermission(permissionId);

            Optional<RolePermission> existingRolePermission = findRolePermission(role, permissionId);

            if (existingRolePermission.isPresent()) {
                RolePermission rolePermission = existingRolePermission.get();

                Set<Long> existingActionIds = rolePermission.getActions().stream()
                        .map(Action::getId)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                Set<Long> mergedActionIds = mergeActionIds(existingActionIds, requestedActionIds, manageActionId);

                rolePermission.setActions(resolveActions(mergedActionIds));
            } else {
                RolePermission rolePermission = RolePermission.builder()
                        .role(role)
                        .permission(permission)
                        .actions(resolveActions(requestedActionIds))
                        .build();

                role.getRolePermissions().add(rolePermission);
            }
        }

        roleRepository.saveAndFlush(role);

        return roleQueryService.findDetail(roleId);
    }

    /**
     * Fully replaces role permissions.
     * <p>
     * This is the best endpoint for admin panel "Save permissions" button.
     * <p>
     * Existing permissions are removed and replaced with request permissions.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS, allEntries = true)
    })
    public RoleDetailResponse replacePermissions(Long roleId, RolePermissionUpdateRequest request) {
        Role role = getAvailableRole(roleId);

        Long manageActionId = resolveManageActionId();
        Map<Long, Set<Long>> requestedPermissions = normalizeRequest(request, manageActionId);

        Set<RolePermission> newRolePermissions = new LinkedHashSet<>();

        for (Map.Entry<Long, Set<Long>> entry : requestedPermissions.entrySet()) {
            Long permissionId = entry.getKey();
            Set<Long> actionIds = entry.getValue();

            Permission permission = getAvailablePermission(permissionId);

            RolePermission rolePermission = RolePermission.builder()
                    .role(role)
                    .permission(permission)
                    .actions(resolveActions(actionIds))
                    .build();

            newRolePermissions.add(rolePermission);
        }

        role.getRolePermissions().clear();
        roleRepository.saveAndFlush(role);

        newRolePermissions.forEach(role::addPermission);
        roleRepository.saveAndFlush(role);

        return roleQueryService.findDetail(roleId);
    }

    /**
     * Removes selected actions from role permissions.
     * <p>
     * Example:
     * Existing: PATIENT -> READ, UPDATE
     * Request : PATIENT -> UPDATE
     * Result  : PATIENT -> READ
     * <p>
     * If no actions remain, RolePermission row is removed.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS, allEntries = true)
    })
    public RoleDetailResponse removePermissions(Long roleId, RolePermissionUpdateRequest request) {
        Role role = getAvailableRole(roleId);

        Long manageActionId = resolveManageActionId();
        Map<Long, Set<Long>> requestedPermissions = normalizeRequest(request, manageActionId);

        for (Map.Entry<Long, Set<Long>> entry : requestedPermissions.entrySet()) {
            Long permissionId = entry.getKey();
            Set<Long> actionIdsToRemove = entry.getValue();

            validatePermissionExists(permissionId);

            Iterator<RolePermission> iterator = role.getRolePermissions().iterator();

            while (iterator.hasNext()) {
                RolePermission rolePermission = iterator.next();

                if (!Objects.equals(rolePermission.getPermission().getId(), permissionId)) {
                    continue;
                }

                if (manageActionId != null && actionIdsToRemove.contains(manageActionId)) {
                    iterator.remove();
                    break;
                }

                boolean hasManage = manageActionId != null && rolePermission.getActions().stream()
                        .anyMatch(action -> Objects.equals(action.getId(), manageActionId));

                if (hasManage) {
                    throw new ConflictException("role.permission.manage_remove_conflict", permissionId);
                }

                rolePermission.getActions().removeIf(action -> actionIdsToRemove.contains(action.getId()));

                if (rolePermission.getActions().isEmpty()) {
                    iterator.remove();
                }

                break;
            }
        }

        roleRepository.saveAndFlush(role);

        return roleQueryService.findDetail(roleId);
    }

    private Role getAvailableRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("role.not_found", roleId));

        if (!role.isAvailableForAuthorization()) {
            throw new ConflictException("role.not_available", roleId);
        }

        return role;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new ConflictException("role.name.required");
        }
        return name.trim();
    }

    private Permission getAvailablePermission(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new NotFoundException("permission.not_found_by_id", permissionId));

        if (!permission.isAvailableForAuthorization()) {
            throw new ConflictException("permission.not_available", permissionId);
        }

        return permission;
    }

    private void validatePermissionExists(Long permissionId) {
        if (!permissionRepository.existsById(permissionId)) {
            throw new NotFoundException("permission.not_found_by_id", permissionId);
        }
    }

    private Action getAvailableAction(Long actionId) {
        Action action = actionRepository.findById(actionId)
                .orElseThrow(() -> new NotFoundException("action.not_found_by_id", actionId));

        if (!action.isAvailableForAuthorization()) {
            throw new ConflictException("action.not_available", actionId);
        }

        return action;
    }

    private Set<Action> resolveActions(Set<Long> actionIds) {
        Set<Action> actions = new LinkedHashSet<>();

        for (Long actionId : actionIds) {
            actions.add(getAvailableAction(actionId));
        }

        return actions;
    }

    private Long resolveManageActionId() {
        return actionRepository.findByCodeIgnoreCase(MANAGE_ACTION_CODE)
                .map(Action::getId)
                .orElse(null);
    }

    private Optional<RolePermission> findRolePermission(Role role, Long permissionId) {
        return role.getRolePermissions()
                .stream()
                .filter(rolePermission ->
                        Objects.equals(rolePermission.getPermission().getId(), permissionId)
                )
                .findFirst();
    }

    /**
     * Normalizes request:
     * <p>
     * 1. Duplicate permissionId values are merged.
     * 2. If the MANAGE action is present, all other actions are ignored.
     * <p>
     * Example:
     * permissionId=1 actionIds=[readId]
     * permissionId=1 actionIds=[updateId]
     * Result: permissionId=1 actionIds=[readId, updateId]
     * <p>
     * Example:
     * permissionId=1 actionIds=[readId, manageId]
     * Result: permissionId=1 actionIds=[manageId]
     */
    private Map<Long, Set<Long>> normalizeRequest(RolePermissionUpdateRequest request, Long manageActionId) {
        if (request == null || request.permissions() == null || request.permissions().isEmpty()) {
            throw new ConflictException("permission.actions.required");
        }

        Map<Long, Set<Long>> normalized = new LinkedHashMap<>();

        for (RolePermissionItemRequest item : request.permissions()) {
            Long permissionId = item.permissionId();

            if (permissionId == null) {
                throw new ConflictException("permission.id.required");
            }

            Set<Long> actionIds = normalizeActions(item.actionIds(), manageActionId);

            normalized.merge(permissionId, actionIds, (existing, incoming) -> mergeActionIds(existing, incoming, manageActionId));
        }

        return normalized;
    }

    private Set<Long> normalizeActions(Set<Long> actionIds, Long manageActionId) {
        if (actionIds == null || actionIds.isEmpty()) {
            throw new ConflictException("permission.actions.required");
        }

        if (manageActionId != null && actionIds.contains(manageActionId)) {
            return new LinkedHashSet<>(Set.of(manageActionId));
        }

        return new LinkedHashSet<>(actionIds);
    }

    private Set<Long> mergeActionIds(
            Set<Long> existingActionIds,
            Set<Long> requestedActionIds,
            Long manageActionId
    ) {
        Set<Long> result = new LinkedHashSet<>();

        if (existingActionIds != null) {
            result.addAll(existingActionIds);
        }

        if (requestedActionIds != null) {
            result.addAll(requestedActionIds);
        }

        if (manageActionId != null && result.contains(manageActionId)) {
            return new LinkedHashSet<>(Set.of(manageActionId));
        }

        return result;
    }
}
