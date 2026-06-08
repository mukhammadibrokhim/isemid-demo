package uz.uzinfocom.app.platform.reference.application.country.query.projection;

public interface CountryTableProjection {

    Long getId();

    String getCode();

    String getNameUz();

    String getNameUzCyril();

    String getNameRu();

    String getNameKaa();

    Boolean getDeleted();
}
