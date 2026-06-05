package uz.uzinfocom.app.platform.reference.application.catalog.query.dto;

import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;

public record CatalogResponse(
        Long id,
        CatalogType type,
        String code,
        String parentCode,
        String nameUz,
        String nameUzCyril,
        String nameRu,
        String nameKaa,
        Boolean deleted,
        Integer sortOrder
) {
}
