package uz.uzinfocom.isemid.features.user.services.impl;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.user.dtos.requests.RoleRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.RoleResponse;
import uz.uzinfocom.isemid.features.user.mappers.RoleMapper;
import uz.uzinfocom.isemid.features.user.models.Permission;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.reposiotries.PermissionRepository;
import uz.uzinfocom.isemid.features.user.reposiotries.RoleRepository;
import uz.uzinfocom.isemid.features.user.services.RoleService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.utils.CommonUtil.createApiResponsePage;
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
    public ApiResponsePage getAllRolesPage(int page, int size, String name, String descriptionUz, String descriptionRu, String descriptionUzCyril, String descriptionKaa, String url) {
        Specification<Role> spec = getFilteredSpecification(name, descriptionUz, descriptionUzCyril, descriptionRu, descriptionKaa);
        Page<Role> rolePage = roleRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, rolePage, url);
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

        Optional.ofNullable(request.getName()).ifPresent(role::setName);
        Optional.ofNullable(request.getIsActive()).ifPresent(role::setActive);
        Optional.ofNullable(request.getPermissionIds()).ifPresent(permissionIds -> {
            Set<Permission> permissions = permissionIds.stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND + ": " + permissionId)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        });
        Optional.ofNullable(request.getDescriptionUz()).ifPresent(role::setDescriptionUz);
        Optional.ofNullable(request.getDescriptionUzCyril()).ifPresent(role::setDescriptionUzCyril);
        Optional.ofNullable(request.getDescriptionRu()).ifPresent(role::setDescriptionRu);
        Optional.ofNullable(request.getDescriptionKaa()).ifPresent(role::setDescriptionKaa);
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, roleMapper.toResponse(roleRepository.save(role)));
    }

    @Override
    public ApiResponse<RoleResponse> deleteById(long id) {
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

        log.info("Default roles and permissions initialized successfully.");
    }


    public static Specification<Role> getFilteredSpecification(
            final String name,
            final String descriptionUz,
            final String descriptionUzCyril,
            final String descriptionRu,
            final String descriptionKaa
    ) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                Stream.of(
                                hasText(name) ? criteriaBuilder.like(root.get("name"), "%" + name + "%") : null,
                                hasText(descriptionUz) ? criteriaBuilder.like(root.get("description"), "%" + descriptionUz + "%") : null,
                                hasText(descriptionUzCyril) ? criteriaBuilder.like(root.get("description"), "%" + descriptionUzCyril + "%") : null,
                                hasText(descriptionRu) ? criteriaBuilder.like(root.get("description"), "%" + descriptionRu + "%") : null,
                                hasText(descriptionKaa) ? criteriaBuilder.like(root.get("description"), "%" + descriptionKaa + "%") : null
                        )
                        .filter(Objects::nonNull)
                        .toArray(Predicate[]::new)
        );
    }

}
