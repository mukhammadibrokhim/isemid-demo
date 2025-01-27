package uz.uzinfocom.isemid.services.impls;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.entities.Permission;
import uz.uzinfocom.isemid.models.entities.Role;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.PermissionRepository;
import uz.uzinfocom.isemid.repositories.RoleRepository;
import uz.uzinfocom.isemid.services.RoleService;

import java.util.*;

import static uz.uzinfocom.isemid.utils.CommonUtils.*;

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

    @Override
    public ApiResponsePage getAllRolesPage(int page, int size, String fullURL, User currentUser) {
        Specification<Role> spec = getFilteredSpecification();
        Page<Role> rolePage = roleRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, rolePage, fullURL);
    }

    @PostConstruct
    public void initDefaultRolesAndPermissions() {
        Map<String, List<String>> rolePermissions = Map.of(
                "isemid_labarant", List.of(),
                "isemid_epidemiologist", List.of(),
                "isemid_head_doctor", List.of(),
                "isemid_super_admin", List.of(),
                "isemid_admin", List.of()
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

    public static Specification<Role> getFilteredSpecification(
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
