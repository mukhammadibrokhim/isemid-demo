package uz.uzinfocom.app.modules.iam.application.action.query.projection;

public interface ActionTableProjection {
    Long getId();

    String getCode();

    Boolean getActive();

    String getDescriptionUz();

    String getDescriptionRu();

    String getDescriptionUzCyril();

    String getDescriptionKaa();
}
