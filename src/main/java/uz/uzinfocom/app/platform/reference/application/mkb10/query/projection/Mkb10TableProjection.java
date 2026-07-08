package uz.uzinfocom.app.platform.reference.application.mkb10.query.projection;

public interface Mkb10TableProjection {

    Long getId();

    Long getParentId();

    String getCode();

    int getLevel();

    boolean isLastLevel();

    String getNameUz();

    String getNameUzCyril();

    String getNameRu();

    String getNameKaa();

    Boolean getDeleted();
}
