package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.Role;

import java.util.List;

public interface RoleService {
    Role findOrCreateRoleByName(String s);

    List<Role> getAllRoles();
}
