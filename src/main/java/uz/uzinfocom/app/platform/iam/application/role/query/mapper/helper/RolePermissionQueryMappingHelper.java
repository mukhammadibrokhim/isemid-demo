package uz.uzinfocom.app.platform.iam.application.role.query.mapper.helper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RolePermissionResponse;
import uz.uzinfocom.app.platform.iam.domain.Permission;
import uz.uzinfocom.app.platform.iam.domain.RolePermission;
import uz.uzinfocom.app.platform.iam.domain.enums.PermissionAction;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RolePermissionQueryMappingHelper {

    private static final Comparator<RolePermission> PERMISSION_COMPARATOR =
            Comparator.comparing(
                    rolePermission ->
                            rolePermission.getPermission().getSubject(),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );

    private final LocalizedTextResolver localizedTextResolver;

    @Named("toRolePermissionResponses")
    public List<RolePermissionResponse> toRolePermissionResponses(
            Collection<RolePermission> rolePermissions
    ) {
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return List.of();
        }

        return rolePermissions.stream()
                .filter(this::hasAvailablePermission)
                .sorted(PERMISSION_COMPARATOR)
                .map(this::toRolePermissionResponse)
                .toList();
    }

    private RolePermissionResponse toRolePermissionResponse(
            RolePermission rolePermission
    ) {
        Permission permission = rolePermission.getPermission();

        return new RolePermissionResponse(
                permission.getId(),
                permission.getSubject(),
                localizedTextResolver.resolve(
                        permission.getDescriptionUz(),
                        permission.getDescriptionUzCyril(),
                        permission.getDescriptionRu(),
                        permission.getDescriptionKaa()
                ),
                toActions(rolePermission.getActions())
        );
    }

    private Set<PermissionAction> toActions(
            Collection<PermissionAction> actions
    ) {
        if (actions == null || actions.isEmpty()) {
            return Set.of();
        }

        EnumSet<PermissionAction> result =
                EnumSet.noneOf(PermissionAction.class);

        actions.stream()
                .filter(Objects::nonNull)
                .forEach(result::add);

        return Collections.unmodifiableSet(result);
    }

    private boolean hasAvailablePermission(
            RolePermission rolePermission
    ) {
        return rolePermission != null
                && rolePermission.getPermission() != null
                && rolePermission.getPermission()
                .isAvailableForAuthorization();
    }
}