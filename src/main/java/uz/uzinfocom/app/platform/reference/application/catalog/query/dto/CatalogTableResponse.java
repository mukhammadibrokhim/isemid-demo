package uz.uzinfocom.app.platform.reference.application.catalog.query.dto;

import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;

public record CatalogTableResponse(
        Long id,
        CatalogType type,
        String code,
        String parentCode,
        String name,
        Integer sortOrder
) {
}
