package uz.uzinfocom.isemid.services.impls;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.entities.Permission;
import uz.uzinfocom.isemid.models.entities.Role;
import uz.uzinfocom.isemid.repositories.PermissionRepository;
import uz.uzinfocom.isemid.repositories.RoleRepository;
import uz.uzinfocom.isemid.services.RoleService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private static final Logger logger = LoggerFactory.getLogger("uz.uzinfocom.isemid");

    final RoleRepository roleRepository;
    final PermissionRepository permissionRepository;

    @Override
    public Role findOrCreateRoleByName(String roleName) {
        Optional<Role> existingRole = roleRepository.findByName(roleName);
        return existingRole.orElseGet(() -> roleRepository.save(Role.builder()
                .name(roleName)
                .isActive(true)
                .build()));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @PostConstruct
    public void initDefaultRolesAndPermissions() {
        Map<String, List<String>> rolePermissions = Map.of(
                "isemid_labarant", List.of(),
                "isemid_epidemiologist", List.of(),
                "isemid_head_doctor", List.of()
        );
        rolePermissions.forEach((roleName, permissions) -> {
            Role role = roleRepository.findByName(roleName).orElseGet(() -> {
                Role newRole = Role.builder()
                        .name(roleName)
                        .isActive(true)
                        .build();
                return roleRepository.save(newRole);
            });

            permissions.forEach(permissionName -> {
                if (role.getPermissions() == null) {
                    role.setPermissions(new HashSet<>());
                }

                if (role.getPermissions().stream().noneMatch(p -> p.getName().equals(permissionName))) {
                    Permission permission = permissionRepository.findByName(permissionName).orElseGet(() -> {
                        Permission newPermission = new Permission();
                        newPermission.setName(permissionName);
                        return permissionRepository.save(newPermission);
                    });
                    role.getPermissions().add(permission);
                }
            });


            roleRepository.save(role);
        });

        logger.info("Default roles and permissions initialized successfully.");
    }

}
