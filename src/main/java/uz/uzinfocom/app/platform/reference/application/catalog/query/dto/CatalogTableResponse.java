package uz.uzinfocom.app.platform.reference.application.catalog.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Catalog reference item for paginated table responses.")
public record CatalogTableResponse(

        @Schema(
                description = "Catalog item internal identifier.",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Catalog type.",
                example = "GENDER"
        )
        String type,

        @Schema(
                description = "Unique item code within the selected catalog type.",
                example = "MALE"
        )
        String code,

        @Schema(
                description = "Optional parent item code within the same catalog type.",
                example = "PERSON",
                nullable = true
        )
        String parentCode,

        @Schema(
                description = "Catalog item name in Uzbek Latin.",
                example = "Erkak"
        )
        String nameUz,

        @Schema(
                description = "Catalog item name in Uzbek Cyrillic.",
                example = "Эркак"
        )
        String nameUzCyril,

        @Schema(
                description = "Catalog item name in Russian.",
                example = "Мужской"
        )
        String nameRu,

        @Schema(
                description = "Catalog item name in Karakalpak.",
                example = "Ер адам"
        )
        String nameKaa
) {
}