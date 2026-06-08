package uz.uzinfocom.app.platform.reference.application.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for updating a Catalog reference record.")
public record CatalogUpdateRequest(
        @Schema(
                description = "Catalog type.",
                example = "GENDER",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "{reference.catalog.type.required}")
        String type,

        @Schema(
                description = "Unique item code inside the selected catalog type.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Optional parent item code inside the same catalog type.")
        @Size(max = 50, message = "{reference.parent_code.max_length}")
        String parentCode,

        @Schema(description = "Catalog item name in Uzbek Latin.")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Catalog item name in Uzbek Cyrillic.")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Catalog item name in Russian.")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Catalog item name in Karakalpak.")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa
) {
}
