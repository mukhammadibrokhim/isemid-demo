package uz.uzinfocom.app.platform.iam.application.role.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleDetailResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleShortResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleTableResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.mapper.helper.RolePermissionQueryMappingHelper;
import uz.uzinfocom.app.platform.iam.application.role.query.mapper.helper.RoleQueryMappingHelper;
import uz.uzinfocom.app.platform.iam.application.role.query.projection.RoleTableProjection;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;
import uz.uzinfocom.app.platform.iam.domain.Role;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                RoleQueryMappingHelper.class,
                RolePermissionQueryMappingHelper.class
        }
)
public interface RoleQueryMapper {

    RoleTableResponse toTableResponse(RoleTableProjection projection);

    @Mapping(target = "audit", source = "audit")
    @Mapping(
            target = "permissions",
            source = "role.rolePermissions",
            qualifiedByName = "toRolePermissionResponses"
    )
    RoleDetailResponse toDetailResponse(
            Role role,
            AuditResponse audit
    );

    @Mapping(
            target = "description",
            source = "role",
            qualifiedByName = "roleDescription"
    )
    RoleShortResponse toShortResponse(Role role);

    @Mapping(
            target = "description",
            source = "projection",
            qualifiedByName = "roleProjectionDescription"
    )
    RoleShortResponse toShortResponse(RoleTableProjection projection);
}