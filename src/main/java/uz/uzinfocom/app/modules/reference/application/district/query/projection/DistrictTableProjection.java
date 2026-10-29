package uz.uzinfocom.app.modules.reference.application.district.query.projection;

public interface DistrictTableProjection {

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
