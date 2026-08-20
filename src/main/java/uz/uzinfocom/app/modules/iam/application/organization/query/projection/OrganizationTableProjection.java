package uz.uzinfocom.app.modules.iam.application.organization.query.projection;

public interface OrganizationTableProjection {
    Long getId();

    String getName();

    String getNameUz();

    String getNameUzCyril();

    String getNameRu();

    String getNameKaa();

    String getRegionCode();

    String getDistrictCode();
}
