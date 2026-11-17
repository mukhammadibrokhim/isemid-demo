package uz.uzinfocom.app.modules.reference.application.country.query.projection;

public interface CountryTableProjection {

    Long getId();

    String getCode();

    String getNameUz();

    String getNameUzCyril();

    String getNameRu();

    String getNameKaa();

    Boolean getDeleted();
}
