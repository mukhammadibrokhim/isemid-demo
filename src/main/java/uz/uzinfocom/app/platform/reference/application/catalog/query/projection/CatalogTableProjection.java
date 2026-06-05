package uz.uzinfocom.app.platform.reference.application.catalog.query.projection;

import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;

public interface CatalogTableProjection {

    Long getId();

    CatalogType getType();

    String getCode();

    String getParentCode();

    String getNameUz();

    String getNameUzCyril();

    String getNameRu();

    String getNameKaa();

    Integer getSortOrder();
}
