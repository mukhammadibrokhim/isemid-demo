package uz.uzinfocom.app.platform.iam.application.user.query.projection;

import uz.uzinfocom.app.platform.iam.application.role.query.projection.RoleTableProjection;

import java.time.LocalDate;
import java.util.Set;

public interface UserTableProjection {
    Long getId();

    String getFirstName();

    String getLastName();

    String getMiddleName();

    String getNnuzb();

    LocalDate getBirthDate();

    String getPhoneNumber();

    Boolean getActive();

    Set<RoleTableProjection> getRoles();
}
