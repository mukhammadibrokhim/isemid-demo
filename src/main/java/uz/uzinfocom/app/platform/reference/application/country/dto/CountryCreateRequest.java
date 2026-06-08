package uz.uzinfocom.app.platform.reference.application.country.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating a Country reference record.")
public record CountryCreateRequest(
        @Schema(
                description = "Unique Country code.",
                example = "UZB",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Country name in Uzbek Latin.", example = "Oʻzbekiston")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Country name in Uzbek Cyrillic.", example = "Ўзбекистон")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Country name in Russian.", example = "Узбекистан")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Country name in Karakalpak.", example = "Ózbekstan")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa
) {
}
