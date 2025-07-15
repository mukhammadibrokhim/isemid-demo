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

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

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
        return createApiResponsePage(page, rolePage.map(roleMapper::toResponse), url);
    }

    @Override
    public ApiResponse<RoleResponse> create(RoleRequest roleRequest) {
        return new ApiResponse<>(true, MessageUtil.CREATED_SUCCESS, roleMapper.toResponse(roleRepository.save(Role.builder()
                .isActive(true)
                .name(roleRequest.getName())
                .descriptionUz(roleRequest.getDescriptionUz())
                .descriptionUzCyril(roleRequest.getDescriptionUzCyril())
                .descriptionRu(roleRequest.getDescriptionRu())
                .descriptionKaa(roleRequest.getDescriptionKaa())
                .permissions(roleRequest.getPermissionIds().stream()
                        .map(permId -> permissionRepository.findById(permId).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND)))
                        .collect(Collectors.toSet()))
                .build())));
    }

    @Override
    public ApiResponse<RoleResponse> update(long id, RoleRequest request) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND));
        roleMapper.updateRole(request, role);
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(permissions);
        }
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, roleMapper.toResponse(roleRepository.save(role)));
    }

    @Override
    public ApiResponse<RoleResponse> deleteById(long id) {
        try {
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_RESOURCE, "Role", id)));

            roleRepository.delete(role);
            return new ApiResponse<>(true, "Роль успешно удалена", null);
        } catch (Exception e) {
            log.error("Error occurred while deleting role with id: {}", id, e);
            throw new ApiException(BAD_REQUEST, "Произошла ошибка при удалении роли: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> roleSearch(String search) {
        List<Role> searchRole = roleRepository.searchRolesNative(search);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, searchRole.stream().map(roleMapper::toResponse).toList());
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
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, roleMapper.toResponse(roleRepository.save(role)));
    }

    @Override
    public ApiResponse<?> removePermission(long roleId, long permissionId) {
        Role role = getRoleOrThrow(roleId);
        Permission permission = getPermissionOrThrow(permissionId);

        Set<Permission> permissions = role.getPermissions();
        if (permissions == null || !permissions.contains(permission)) {
            return new ApiResponse<>(false, "Permission не найдена в роли", null);
        }

        permissions.remove(permission);
        role.setPermissions(permissions);
        Role savedRole = roleRepository.save(role);

        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, roleMapper.toResponse(savedRole));
    }

    private Role getRoleOrThrow(long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_RESOURCE, "Role", roleId)));
    }

    private Permission getPermissionOrThrow(long permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_RESOURCE, "Permission", permissionId)));
    }
}
