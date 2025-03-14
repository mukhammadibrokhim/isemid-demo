package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.Role;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.requests.ReqRole;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

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
            String url,
            User currentUser);

    ApiResponse<Role> create(ReqRole reqRole);

    ApiResponse<Role> update(long id, ReqRole reqRole);

    ApiResponse<Role> deleteById(long id);

}
