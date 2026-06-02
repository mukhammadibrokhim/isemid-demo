package uz.uzinfocom.app.platform.iam.application.role.query.mapper;

import org.springframework.stereotype.Component;
import org.mapstruct.Named;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionDetailResponse;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleResponse;
import uz.uzinfocom.app.platform.iam.domain.Permission;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.RolePermission;
import uz.uzinfocom.app.platform.iam.domain.enums.PermissionAction;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RolePermissionResponseMapper {

    public RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getActive(),
                toPermissionResponses(role)
        );
    }

    public List<RoleResponse> toRoleResponses(Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Role::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::toRoleResponse)
                .toList();
    }

    @Named("toPermissionResponses")
    public List<PermissionResponse> toPermissionResponses(Role role) {
        if (role == null || role.getRolePermissions() == null || role.getRolePermissions().isEmpty()) {
            return List.of();
        }

        Map<Long, PermissionResponse> byPermissionId = new LinkedHashMap<>();

        role.getRolePermissions().stream()
                .filter(Objects::nonNull)
                .filter(this::hasAvailablePermission)
                .sorted(Comparator.comparing(
                        rolePermission -> rolePermission.getPermission().getSubject(),
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .forEach(rolePermission -> {
                    Permission permission = rolePermission.getPermission();
                    byPermissionId.put(
                            permission.getId(),
                            new PermissionResponse(
                                    permission.getId(),
                                    permission.getSubject(),
                                    actionNames(rolePermission.getActions())
                            )
                    );
                });

        return List.copyOf(byPermissionId.values());
    }

    @Named("toPermissionDetailResponses")
    public List<PermissionDetailResponse> toPermissionDetailResponses(Role role) {
        if (role == null || role.getRolePermissions() == null || role.getRolePermissions().isEmpty()) {
            return List.of();
        }

        return role.getRolePermissions().stream()
                .filter(Objects::nonNull)
                .filter(this::hasAvailablePermission)
                .sorted(Comparator.comparing(
                        rolePermission -> rolePermission.getPermission().getSubject(),
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .map(rolePermission -> {
                    Permission permission = rolePermission.getPermission();
                    return new PermissionDetailResponse(
                            permission.getId(),
                            permission.getSubject(),
                            permission.getDescriptionUz(),
                            permission.getDescriptionRu(),
                            permission.getDescriptionUzCyril(),
                            permission.getDescriptionKaa(),
                            actionNames(rolePermission.getActions())
                    );
                })
                .toList();
    }

    public List<PermissionResponse> mergeEffectivePermissions(Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        Map<Long, EffectivePermission> byPermissionId = new LinkedHashMap<>();

        for (Role role : roles) {
            if (role == null || role.getRolePermissions() == null) {
                continue;
            }

            for (RolePermission rolePermission : role.getRolePermissions()) {
                if (!hasAvailablePermission(rolePermission)) {
                    continue;
                }

                Permission permission = rolePermission.getPermission();
                EffectivePermission effective = byPermissionId.computeIfAbsent(
                        permission.getId(),
                        id -> new EffectivePermission(permission.getId(), permission.getSubject())
                );
                effective.actions.addAll(actionNames(rolePermission.getActions()));
            }
        }

        return byPermissionId.values()
                .stream()
                .sorted(Comparator.comparing(EffectivePermission::subject, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(permission -> new PermissionResponse(
                        permission.id(),
                        permission.subject(),
                        Set.copyOf(permission.actions())
                ))
                .toList();
    }

    public Set<String> actionNames(Set<PermissionAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return Set.of();
        }

        return actions.stream()
                .filter(Objects::nonNull)
                .map(Enum::name)
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean hasAvailablePermission(RolePermission rolePermission) {
        if (rolePermission == null || rolePermission.getPermission() == null) {
            return false;
        }

        return rolePermission.getPermission().isAvailableForAuthorization();
    }

    private record EffectivePermission(Long id, String subject, Set<String> actions) {
        private EffectivePermission(Long id, String subject) {
            this(id, subject, new LinkedHashSet<>());
        }
    }
}
