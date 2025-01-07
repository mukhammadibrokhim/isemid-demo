package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.Role;

public interface RoleService {
    Role findOrCreateRoleByName(String s);
}
