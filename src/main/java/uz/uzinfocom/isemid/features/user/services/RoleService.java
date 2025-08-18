package uz.uzinfocom.isemid.features.user.services;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.user.dtos.requests.RoleRequest;
import uz.uzinfocom.isemid.features.user.models.Role;

import java.util.List;

public interface RoleService {
    Page<Role> filter(
            int page,
            int size,
            String name,
            String descriptionUz,
            String descriptionRu,
            String descriptionUzCyril,
            String descriptionKaa
    );

    Role findOrCreateRoleByName(String s);

    List<Role> getAllRoles();

    Role create(RoleRequest reqRole);

    Role update(long id, RoleRequest roleRequest);

    void deleteById(long id);

    List<Role> roleSearch(String search);

    Role addPermission(long roleId, long permissionId);

    Role removePermission(long roleId, long permissionId);
}
