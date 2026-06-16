package uz.uzinfocom.app.platform.reference.application.country.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed Country reference response.")
public record CountryDetailedResponse(

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

        @Schema(
                description = "ISO 3166-1 alpha-2 country code.",
                example = "UZ"
        )
        String alpha2Code,

        @Schema(
                description = "ISO 3166-2 country subdivision prefix.",
                example = "UZ"
        )
        String iso3166Part2Code,

        @Schema(
                description = "Country citizenship classifier code.",
                example = "860"
        )
        String citizenshipCode,

        @Schema(description = "Soft-delete flag.", example = "false")
        Boolean deleted
) {
}