package uz.uzinfocom.app.modules.iam.application.permission.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.iam.application.permission.query.dto.PermissionDetailResponse;
import uz.uzinfocom.app.modules.iam.application.permission.query.dto.PermissionTableResponse;
import uz.uzinfocom.app.modules.iam.application.permission.query.projection.PermissionTableProjection;
import uz.uzinfocom.app.platform.persistence.audit.AuditResponse;
import uz.uzinfocom.app.modules.iam.domain.Permission;

@Mapper(componentModel = "spring")
public interface PermissionQueryMapper {

    PermissionTableResponse toTableResponse(PermissionTableProjection permissionTableProjection);

    PermissionTableResponse toTableResponse(Permission permission);

    @Mapping(target = "audit", source = "audit")
    PermissionDetailResponse toDetailResponse(Permission permission, AuditResponse audit);
}
