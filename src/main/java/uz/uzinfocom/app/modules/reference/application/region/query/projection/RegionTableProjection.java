package uz.uzinfocom.app.modules.reference.application.region.query.projection;

public interface RegionTableProjection {

    Long getId();

    String getCode();

    String getParentCode();

    Integer getSoatoId();

    String getNameUz();

    String getNameUzCyril();

    String getNameRu();

    String getNameKaa();

    Boolean getDeleted();
}
