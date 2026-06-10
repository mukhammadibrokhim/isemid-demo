package uz.uzinfocom.app.platform.iam.application.role.query.mapper.helper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.projection.RoleTableProjection;
import uz.uzinfocom.app.platform.iam.domain.Role;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RoleQueryMappingHelper {

    private static final Comparator<Role> ROLE_COMPARATOR =
            Comparator.comparing(
                    Role::getName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );

    private final LocalizedTextResolver localizedTextResolver;
    private final RolePermissionQueryMappingHelper rolePermissionMappingHelper;

    @Named("roleDescription")
    public String roleDescription(Role role) {
        if (role == null) {
            return null;
        }

        return localizedTextResolver.resolve(
                role.getDescriptionUz(),
                role.getDescriptionUzCyril(),
                role.getDescriptionRu(),
                role.getDescriptionKaa()
        );
    }

    @Named("roleProjectionDescription")
    public String roleProjectionDescription(
            RoleTableProjection projection
    ) {
        if (projection == null) {
            return null;
        }

        return localizedTextResolver.resolve(
                projection.getDescriptionUz(),
                projection.getDescriptionUzCyril(),
                projection.getDescriptionRu(),
                projection.getDescriptionKaa()
        );
    }

    @Named("toRoleResponses")
    public List<RoleResponse> toRoleResponses(Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .filter(Objects::nonNull)
                .filter(Role::isAvailableForAuthorization)
                .sorted(ROLE_COMPARATOR)
                .map(this::toRoleResponse)
                .toList();
    }

    private RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getActive(),
                rolePermissionMappingHelper.toRolePermissionResponses(
                        role.getRolePermissions()
                )
        );
    }
}