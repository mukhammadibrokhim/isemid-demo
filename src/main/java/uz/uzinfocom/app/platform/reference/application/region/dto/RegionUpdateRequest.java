package uz.uzinfocom.app.platform.reference.application.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для обновления записи региона.")
public record RegionUpdateRequest(
        @Schema(
                description = "Уникальный код региона.",
                example = "UZ-AN",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(
                description = "Код родительской страны для данного региона.",
                example = "UZ",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.parent_code.required}")
        @Size(max = 50, message = "{reference.parent_code.max_length}")
        String parentCode,

        @Schema(description = "Наименование региона на узбекском языке (латиница).", example = "Andijon viloyati")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Наименование региона на узбекском языке (кириллица).", example = "Андижон вилояти")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Наименование региона на русском языке.", example = "Андижанская область")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Наименование региона на каракалпакском языке.", example = "Andijan wálayatı")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa
) {
}
