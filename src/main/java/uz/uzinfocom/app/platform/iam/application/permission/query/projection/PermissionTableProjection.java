package uz.uzinfocom.app.platform.iam.application.permission.query.projection;

public interface PermissionTableProjection {
    Long getId();

    String getSubject();

    Boolean getActive();

    String getDescriptionUz();

    String getDescriptionRu();

    String getDescriptionUzCyril();

    String getDescriptionKaa();
}
