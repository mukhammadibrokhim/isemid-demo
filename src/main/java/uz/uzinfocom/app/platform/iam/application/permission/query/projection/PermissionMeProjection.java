package uz.uzinfocom.app.platform.iam.application.permission.query.projection;

import java.util.Set;

public interface PermissionMeProjection {

    Long getId();

    String getSubject();

    Boolean getActive();

    Boolean getDeleted();

    Set<String> getActions();
}