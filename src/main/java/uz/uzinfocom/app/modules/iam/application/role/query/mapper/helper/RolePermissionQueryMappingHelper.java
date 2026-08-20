package uz.uzinfocom.app.modules.iam.application.role.query.mapper.helper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.modules.iam.application.role.query.dto.RolePermissionActionResponse;
import uz.uzinfocom.app.modules.iam.application.role.query.dto.RolePermissionResponse;
import uz.uzinfocom.app.modules.iam.domain.Action;
import uz.uzinfocom.app.modules.iam.domain.Permission;
import uz.uzinfocom.app.modules.iam.domain.RolePermission;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Component
@RequiredArgsConstructor
public class RolePermissionQueryMappingHelper {

    private static final Comparator<RolePermission> PERMISSION_COMPARATOR =
            Comparator.comparing(
                    rolePermission ->
                            rolePermission.getPermission().getSubject(),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );

    private static final Comparator<Action> ACTION_COMPARATOR =
            Comparator.comparing(Action::getCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

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

    private Set<RolePermissionActionResponse> toActions(
            Collection<Action> actions
    ) {
        if (actions == null || actions.isEmpty()) {
            return Set.of();
        }

        Set<Action> sorted = new TreeSet<>(ACTION_COMPARATOR);
        actions.stream()
                .filter(Objects::nonNull)
                .filter(Action::isAvailableForAuthorization)
                .forEach(sorted::add);

        Set<RolePermissionActionResponse> result = new LinkedHashSet<>();
        for (Action action : sorted) {
            result.add(new RolePermissionActionResponse(
                    action.getId(),
                    action.getCode(),
                    localizedTextResolver.resolve(
                            action.getDescriptionUz(),
                            action.getDescriptionUzCyril(),
                            action.getDescriptionRu(),
                            action.getDescriptionKaa()
                    )
            ));
        }

        return result;
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