package uz.uzinfocom.app.platform.reference.application.catalog.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

@Schema(description = "Detailed Catalog reference response.")
public record CatalogResponse(
        @Schema(description = "Catalog item internal identifier.", example = "1")
        Long id,
        @Schema(description = "Catalog type.", example = "GENDER")
        String type,
        @Schema(description = "Unique item code inside the selected catalog type.")
        String code,
        @Schema(description = "Optional parent item code inside the same catalog type.")
        String parentCode,
        @Schema(description = "Catalog item name in Uzbek Latin.")
        String nameUz,
        @Schema(description = "Catalog item name in Uzbek Cyrillic.")
        String nameUzCyril,
        @Schema(description = "Catalog item name in Russian.")
        String nameRu,
        @Schema(description = "Catalog item name in Karakalpak.")
        String nameKaa,
        @Schema(description = "Soft-delete flag.", example = "false")
        Boolean deleted,
        AuditResponse audit
) {
}
