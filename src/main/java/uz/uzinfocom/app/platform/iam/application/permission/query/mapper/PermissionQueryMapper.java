package uz.uzinfocom.app.platform.iam.application.permission.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionDetailResponse;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionTableResponse;
import uz.uzinfocom.app.platform.iam.application.permission.query.projection.PermissionTableProjection;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;
import uz.uzinfocom.app.platform.iam.domain.Permission;

@Mapper(componentModel = "spring")
public interface PermissionQueryMapper {

    PermissionTableResponse toTableResponse(PermissionTableProjection permissionTableProjection);

    PermissionTableResponse toTableResponse(Permission permission);

    @Mapping(target = "audit", source = "audit")
    PermissionDetailResponse toDetailResponse(Permission permission, AuditResponse audit);
}
