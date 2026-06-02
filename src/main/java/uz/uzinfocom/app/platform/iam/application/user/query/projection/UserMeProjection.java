package uz.uzinfocom.app.platform.iam.application.user.query.projection;

import uz.uzinfocom.app.platform.iam.application.role.query.projection.RolePermissionMeProjection;

import java.util.Set;
import java.util.UUID;


public interface UserMeProjection {

    Long getId();

    UUID getUuid();

    String getFirstName();

    String getLastName();

    String getMiddleName();

    Boolean getActive();

    Set<RolePermissionMeProjection> getRoles();
}
