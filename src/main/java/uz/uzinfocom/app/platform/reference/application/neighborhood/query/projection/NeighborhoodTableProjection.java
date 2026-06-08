package uz.uzinfocom.app.platform.reference.application.neighborhood.query.projection;

public interface NeighborhoodTableProjection {

    Long getId();

    String getCode();

    String getParentCode();

    Integer getSoatoId();

    Integer getParentSoatoId();

    String getNameUz();

    String getNameUzCyril();

    String getNameRu();

    String getNameKaa();

    Boolean getDeleted();
}
