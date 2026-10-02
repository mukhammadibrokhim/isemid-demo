package uz.uzinfocom.app.modules.iam.application.organization.query.projection;

import java.util.UUID;

public interface OrganizationTableProjection {
    Long getId();

    UUID getUuid();

    String getTin();

    Boolean getActive();

    String getName();

    String getNameUz();

    String getNameUzCyril();

    String getNameRu();

    String getNameKaa();

    String getRegionCode();

    String getDistrictCode();
}
