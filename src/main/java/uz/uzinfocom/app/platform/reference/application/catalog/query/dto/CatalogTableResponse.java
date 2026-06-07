package uz.uzinfocom.app.platform.reference.application.catalog.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;

@Schema(description = "Catalog reference row for paginated table responses.")
public record CatalogTableResponse(
        @Schema(description = "Catalog item internal identifier.", example = "1")
        Long id,
        @Schema(description = "Catalog type.", example = "GENDER")
        CatalogType type,
        @Schema(description = "Unique item code inside the selected catalog type.")
        String code,
        @Schema(description = "Optional parent item code inside the same catalog type.")
        String parentCode,
        @Schema(description = "Localized catalog item name resolved for the current locale.")
        String name,
        @Schema(description = "Display order for Catalog records.", example = "10")
        Integer sortOrder
) {
}
