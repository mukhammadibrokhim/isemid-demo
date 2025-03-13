package uz.uzinfocom.isemid.services.impls;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.Permission;
import uz.uzinfocom.isemid.models.entities.Role;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.PermissionRepository;
import uz.uzinfocom.isemid.repositories.RoleRepository;
import uz.uzinfocom.isemid.services.RoleService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.utils.CommonUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.utils.CommonUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private static final Logger logger = LoggerFactory.getLogger("uz.uzinfocom.isemid");

    final RoleRepository roleRepository;
    final PermissionRepository permissionRepository;

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
    public ApiResponsePage getAllRolesPage(int page, int size, String name, String descriptionUz, String descriptionRu, String descriptionUzCyril, String descriptionKaa, String url, User currentUser) {
        Specification<Role> spec = getFilteredSpecification(name, descriptionUz, descriptionUzCyril, descriptionRu, descriptionKaa);
        Page<Role> rolePage = roleRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, rolePage, url);
    }

    @Override
    public ApiResponse<Role> roleAddPermission(long roleId, String permissionName) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ApiException(NOT_FOUND, "Role not found with id: " + roleId));
        Permission permission = permissionRepository.findByName(permissionName).orElseThrow(() -> new ApiException(NOT_FOUND, "Permission not found with name: " + permissionName));
        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }

        if (role.getPermissions().contains(permission)) {
            throw new ApiException(BAD_REQUEST, "Permission already added to the role" + permissionName);
        }

        role.getPermissions().add(permission);

        return new ApiResponse<>(true, "Success", roleRepository.save(role));
    }

    @Override
    public ApiResponse<Role> roleRemovePermission(long roleId, String permissionName) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Role not found with id: " + roleId));

        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Permission not found with name: " + permissionName));

        if (role.getPermissions() == null || !role.getPermissions().contains(permission)) {
            throw new ApiException(BAD_REQUEST, "Permission not found in the role" + permissionName);
        }

        role.getPermissions().remove(permission);

        return new ApiResponse<>(true, "Permission successfully removed from the role", roleRepository.save(role));
    }

    @PostConstruct
    public void initDefaultRolesAndPermissions() {
        Map<String, Map<String, Object>> rolePermissions = Map.of(
                "isemid_qr_user", Map.of(
                        "descriptionUz", "QR foydalanuvchi roli",
                        "descriptionUzCyril", "QR фойдаланувчи роли",
                        "descriptionRu", "Роль пользователя QR",
                        "descriptionKaa", "QR колдонучунун ролу",
                        "permission", List.of()),
                "isemid_epidemiologist", Map.of(
                        "descriptionUz", "Epidemiolog rol",
                        "descriptionUzCyril", "Эпидемиолог рол",
                        "descriptionRu", "Роль эпидемиолога",
                        "descriptionKaa", "Эпидемиология кызматы",
                        "permission", List.of()),
                "isemid_head_doctor", Map.of(
                        "descriptionUz", "Bosh shifokor rol",
                        "descriptionUzCyril", "Бош шифокор рол",
                        "descriptionRu", "Роль главного врача",
                        "descriptionKaa", "Башкы врач",
                        "permission", List.of()),
                "isemid_epidim_head", Map.of(
                        "descriptionUz", "Epidemiologiya va immunoprofilaktika bo‘limi mudiri",
                        "descriptionUzCyril", "Эпидемиология ва иммунопрофилактика бўлими мудири",
                        "descriptionRu", "Заведующий отделом эпидемиологии и иммунопрофилактики",
                        "descriptionKaa", "Эпидемиология жана иммунопрофилактика бөлүмүнүн мудири",
                        "permission", List.of()),
                "isemid_general_practitioner", Map.of(
                        "descriptionUz", "Umumiy amaliyot shifokori",
                        "descriptionUzCyril", "Умумий амалийот шифокори",
                        "descriptionRu", "Врач",
                        "descriptionKaa", "Жалпы практика дарыгери",
                        "permission", List.of()),
                "isemid_super_admin", Map.of(
                        "descriptionUz", "Super admin rol",
                        "descriptionUzCyril", "Супер админ рол",
                        "descriptionRu", "Роль супер администратора",
                        "descriptionKaa", "Супер администратор",
                        "permission", List.of()),
                "isemid_assistant_epidemiologist", Map.of(
                        "descriptionUz", "Epidemiolog shifokor yordamchisi",
                        "descriptionUzCyril", "Эпидемиолог шифокор ёрдамчиси",
                        "descriptionRu", "Помощник врача-эпидемиолога",
                        "descriptionKaa", "Epidemiolog-dáriger kómekshisi",
                        "permission", List.of()
                ),
                "isemid_admin", Map.of(
                        "descriptionUz", "Admin rol",
                        "descriptionUzCyril", "Админ рол",
                        "descriptionRu", "Роль администратора",
                        "descriptionKaa", "Администратор",
                        "permission", List.of())
        );
        rolePermissions.forEach((roleName, roleData) -> {
            String descriptionUz = (String) roleData.get("descriptionUz");
            String descriptionUzCyril = (String) roleData.get("descriptionUzCyril");
            String descriptionRu = (String) roleData.get("descriptionRu");
            String descriptionKaa = (String) roleData.get("descriptionKaa");
            List<String> permissionsList = (List<String>) roleData.get("permissions");
            if (permissionsList == null) {
                permissionsList = new ArrayList<>();
            }
            Role role = roleRepository.findByName(roleName).orElseGet(() -> {
                Role newRole = Role.builder()
                        .name(roleName)
                        .isActive(true)
                        .descriptionUz(descriptionUz)
                        .descriptionUzCyril(descriptionUzCyril)
                        .descriptionRu(descriptionRu)
                        .descriptionKaa(descriptionKaa)
                        .build();
                return roleRepository.save(newRole);
            });

            permissionsList.forEach(permissionName -> {
                Permission permission = permissionRepository.findByName(permissionName).orElseGet(() -> {
                    Permission newPermission = new Permission();
                    newPermission.setName(permissionName);
                    return permissionRepository.save(newPermission);
                });

                if (role.getPermissions() == null) {
                    role.setPermissions(new HashSet<>());
                }
                role.getPermissions().add(permission);
            });

            roleRepository.save(role);
        });

        logger.info("Default roles and permissions initialized successfully.");
    }

    public static Specification<Role> getFilteredSpecification(
            final String name,
            final String descriptionUz,
            final String descriptionUzCyril,
            final String descriptionRu,
            final String descriptionKaa
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(name)) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            if (hasText(descriptionUz)) {
                predicates.add(criteriaBuilder.like(root.get("description"), "%" + descriptionUz + "%"));
            }
            if (hasText(descriptionUzCyril)) {
                predicates.add(criteriaBuilder.like(root.get("description"), "%" + descriptionUzCyril + "%"));
            }
            if (hasText(descriptionRu)) {
                predicates.add(criteriaBuilder.like(root.get("description"), "%" + descriptionRu + "%"));
            }
            if (hasText(descriptionKaa)) {
                predicates.add(criteriaBuilder.like(root.get("description"), "%" + descriptionKaa + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
