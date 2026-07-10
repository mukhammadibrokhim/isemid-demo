package uz.uzinfocom.app.platform.reference.application.country.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для обновления записи страны.")
public record CountryUpdateRequest(
        @Schema(
                description = "Уникальный код страны.",
                example = "UZB",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Наименование страны на узбекском языке (латиница).", example = "Oʻzbekiston")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Наименование страны на узбекском языке (кириллица).", example = "Ўзбекистон")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Наименование страны на русском языке.", example = "Узбекистан")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Наименование страны на каракалпакском языке.", example = "Ózbekstan")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa
) {
}
