package uz.uzinfocom.isemid.features.user.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.PagedResponseUtil;
import uz.uzinfocom.isemid.features.user.dtos.requests.RoleRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.RoleResponse;
import uz.uzinfocom.isemid.features.user.mappers.RoleMapper;
import uz.uzinfocom.isemid.features.user.models.Permission;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.reposiotries.PermissionRepository;
import uz.uzinfocom.isemid.features.user.reposiotries.RoleRepository;
import uz.uzinfocom.isemid.features.user.services.RoleService;
import uz.uzinfocom.isemid.features.user.specifications.RoleSpecification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final MessageUtil messageUtil;

    @Override
    public Role findOrCreateRoleByName(String roleName) {
        if (!roleName.contains("isemid")) return null;
        return roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(Role.builder()
                .name(roleName)
                .isActive(true)
                .build()));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public PagedResponse<?> getAllRolesPage(int page, int size, String name, String descriptionUz, String descriptionRu, String descriptionUzCyril, String descriptionKaa, String url) {
        Specification<Role> spec = RoleSpecification.getFilteredSpecification(name, descriptionUz, descriptionUzCyril, descriptionRu, descriptionKaa);
        Page<Role> rolePage = roleRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return PagedResponseUtil.build(page, rolePage.map(roleMapper::toResponse), url, messageUtil);
    }

    @Override
    public ApiResponse<RoleResponse> create(RoleRequest roleRequest) {
        return ApiResponse.success(messageUtil.get("message.success"), roleMapper.toResponse(roleRepository.save(Role.builder()
                .isActive(true)
                .name(roleRequest.getName())
                .descriptionUz(roleRequest.getDescriptionUz())
                .descriptionUzCyril(roleRequest.getDescriptionUzCyril())
                .descriptionRu(roleRequest.getDescriptionRu())
                .descriptionKaa(roleRequest.getDescriptionKaa())
                .permissions(roleRequest.getPermissionIds().stream()
                        .map(permId -> permissionRepository.findById(permId).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", permId))))
                        .collect(Collectors.toSet()))
                .build())));
    }

    @Override
    public ApiResponse<RoleResponse> update(long id, RoleRequest request) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        roleMapper.updateRole(request, role);
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(permissions);
        }
        return ApiResponse.success(messageUtil.get("message.success"), roleMapper.toResponse(roleRepository.save(role)));
    }

    @Override
    public ApiResponse<?> deleteById(long id) {
        try {
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));

            roleRepository.delete(role);
            return ApiResponse.success(messageUtil.get("message.success"));
        } catch (Exception e) {
            log.error("Error occurred while deleting role with id: {}", id, e);
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.internal_server") + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> roleSearch(String search) {
        List<Role> searchRole = roleRepository.searchRolesNative(search);
        return ApiResponse.success(messageUtil.get("message.success"), searchRole.stream().map(roleMapper::toResponse).toList());
    }

    @Override
    public ApiResponse<?> addPermission(long roleId, long permissionId) {
        Role role = getRoleOrThrow(roleId);
        Permission permission = getPermissionOrThrow(permissionId);

        Set<Permission> permissions = role.getPermissions();
        if (permissions == null) {
            permissions = new HashSet<>();
        }
        if (permissions.contains(permission)) {
            throw new ApiException(BAD_REQUEST, "Permission уже добавлен в роль");
        }
        permissions.add(permission);
        role.setPermissions(permissions);
        return ApiResponse.success(messageUtil.get("message.success"), roleMapper.toResponse(roleRepository.save(role)));
    }

    @Override
    public ApiResponse<?> removePermission(long roleId, long permissionId) {
        Role role = getRoleOrThrow(roleId);
        Permission permission = getPermissionOrThrow(permissionId);

        Set<Permission> permissions = role.getPermissions();
        if (permissions == null || !permissions.contains(permission)) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.not_found_by_id", permissionId));
        }

        permissions.remove(permission);
        role.setPermissions(permissions);
        Role savedRole = roleRepository.save(role);

        return ApiResponse.success(messageUtil.get("message.success"), roleMapper.toResponse(savedRole));
    }

    private Role getRoleOrThrow(long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", roleId)));
    }

    private Permission getPermissionOrThrow(long permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", permissionId)));
    }
}
