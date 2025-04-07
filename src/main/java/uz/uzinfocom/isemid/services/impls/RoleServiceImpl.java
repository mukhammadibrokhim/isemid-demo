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
import uz.uzinfocom.isemid.models.payload.requests.ReqRole;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.PermissionRepository;
import uz.uzinfocom.isemid.repositories.RoleRepository;
import uz.uzinfocom.isemid.services.RoleService;

import java.util.*;
import java.util.stream.Collectors;

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
    public ApiResponse<Role> create(ReqRole reqRole) {
        return new ApiResponse<>(true, "Success", roleRepository.save(Role.builder()
                .isActive(true)
                .name(reqRole.getName())
                .descriptionUz(reqRole.getDescriptionUz())
                .descriptionUzCyril(reqRole.getDescriptionUzCyril())
                .descriptionRu(reqRole.getDescriptionRu())
                .descriptionKaa(reqRole.getDescriptionKaa())
                .permissions(reqRole.getPermissionIds().stream()
                        .map(permId -> permissionRepository.findById(permId).orElseThrow(() -> new ApiException(NOT_FOUND, "Permission not found with id: " + permId)))
                        .collect(Collectors.toSet()))
                .build()));
    }

    @Override
    public ApiResponse<Role> update(long id, ReqRole reqRole) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Role not found with id: " + id));

        Optional.ofNullable(reqRole.getName()).ifPresent(role::setName);
        Optional.ofNullable(reqRole.getIsActive()).ifPresent(role::setActive);
        Optional.ofNullable(reqRole.getPermissionIds()).ifPresent(permissionIds -> {
            Set<Permission> permissions = permissionIds.stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new ApiException(NOT_FOUND, "Permission not found with id: " + permissionId)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        });
        Optional.ofNullable(reqRole.getDescriptionUz()).ifPresent(role::setDescriptionUz);
        Optional.ofNullable(reqRole.getDescriptionUzCyril()).ifPresent(role::setDescriptionUzCyril);
        Optional.ofNullable(reqRole.getDescriptionRu()).ifPresent(role::setDescriptionRu);
        Optional.ofNullable(reqRole.getDescriptionKaa()).ifPresent(role::setDescriptionKaa);
        return new ApiResponse<>(true, "Success", roleRepository.save(role));
    }

    @Override
    public ApiResponse<Role> deleteById(long id) {
        return null;
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
                        "descriptionUz", "Xabar beruvchi tibbiy xodim",
                        "descriptionUzCyril", "Хабар берувчи тиббий ходим",
                        "descriptionRu", "Информирующий медицинский работник",
                        "descriptionKaa", "Xabar beretuǵın medicinalıq xızmetker",
                        "permission", List.of()),
                "isemid_super_admin", Map.of(
                        "descriptionUz", "Super admin rol",
                        "descriptionUzCyril", "Супер админ рол",
                        "descriptionRu", "Роль супер администратора",
                        "descriptionKaa", "Супер администратор",
                        "permission", List.of("ALL")),
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
                        "permission", List.of("ALL"))
        );
        rolePermissions.forEach((roleName, roleData) -> {
            String descriptionUz = (String) roleData.get("descriptionUz");
            String descriptionUzCyril = (String) roleData.get("descriptionUzCyril");
            String descriptionRu = (String) roleData.get("descriptionRu");
            String descriptionKaa = (String) roleData.get("descriptionKaa");
            List<String> permissionsList = (List<String>) roleData.get("permission");
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

            for (String permissionName : permissionsList) {
                Permission permission = permissionRepository.findBySubject(permissionName).orElseGet(() -> {
                    Permission newPermission = new Permission();
                    newPermission.setSubject(permissionName);
                    newPermission.setActions(List.of("manager"));
                    return permissionRepository.save(newPermission);
                });

                if (role.getPermissions() == null) {
                    role.setPermissions(new HashSet<>());
                }
                role.getPermissions().add(permission);
            }

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
