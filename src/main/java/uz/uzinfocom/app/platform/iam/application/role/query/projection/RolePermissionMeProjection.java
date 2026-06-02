package uz.uzinfocom.app.platform.iam.application.role.query.projection;

import uz.uzinfocom.app.platform.iam.application.permission.query.projection.PermissionMeProjection;
import uz.uzinfocom.app.platform.iam.domain.enums.PermissionAction;

import java.util.Set;

public interface RolePermissionMeProjection {

    PermissionMeProjection getPermission();

    Set<PermissionAction> getActions();
}