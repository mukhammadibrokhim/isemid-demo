package uz.uzinfocom.app.platform.reference.application.country.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Country reference row for paginated table responses.")
public record CountryTableResponse(
        @Schema(description = "Country internal identifier.", example = "240")
        Long id,
        @Schema(description = "Unique Country code.", example = "UZB")
        String code,
        @Schema(description = "Country name in Uzbek Latin.", example = "Oʻzbekiston")
        String nameUz,
        @Schema(description = "Country name in Uzbek Cyrillic.", example = "Ўзбекистон")
        String nameUzCyril,
        @Schema(description = "Country name in Russian.", example = "Узбекистан")
        String nameRu,
        @Schema(description = "Country name in Karakalpak.", example = "Ózbekstan")
        String nameKaa,
        @Schema(description = "Soft-delete flag.", example = "false")
        Boolean deleted,
        @Schema(description = "Display order for Country records.", example = "0")
        Integer sortOrder
) {
}
