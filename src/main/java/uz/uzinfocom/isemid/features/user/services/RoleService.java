package uz.uzinfocom.isemid.features.user.services;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.features.user.dtos.requests.RoleRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.RoleResponse;
import uz.uzinfocom.isemid.features.user.models.Role;

import java.util.List;

public interface RoleService {
    Role findOrCreateRoleByName(String s);

    List<Role> getAllRoles();

    ApiResponsePage getAllRolesPage(
            int page,
            int size,
            String name,
            String descriptionUz,
            String descriptionRu,
            String descriptionUzCyril,
            String descriptionKaa,
            String url);

    ApiResponse<RoleResponse> create(RoleRequest reqRole);

    ApiResponse<RoleResponse> update(long id, RoleRequest roleRequest);

    ApiResponse<RoleResponse> deleteById(long id);

    ApiResponse<?> roleSearch(String search);

    ApiResponse<?> addPermission(long roleId, long permissionId);

    ApiResponse<?> removePermission(long roleId, long permissionId);
}
