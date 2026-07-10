package uz.uzinfocom.app.platform.reference.application.country.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Детальный ответ по стране.")
public record CountryDetailedResponse(

        @Schema(description = "Внутренний идентификатор страны.", example = "240")
        Long id,

        @Schema(description = "Уникальный код страны.", example = "UZB")
        String code,

        @Schema(description = "Наименование страны на узбекском языке (латиница).", example = "Oʻzbekiston")
        String nameUz,

        @Schema(description = "Наименование страны на узбекском языке (кириллица).", example = "Ўзбекистон")
        String nameUzCyril,

        @Schema(description = "Наименование страны на русском языке.", example = "Узбекистан")
        String nameRu,

        @Schema(description = "Наименование страны на каракалпакском языке.", example = "Ózbekstan")
        String nameKaa,

        @Schema(
                description = "Код страны по ISO 3166-1 alpha-2.",
                example = "UZ"
        )
        String alpha2Code,

        @Schema(
                description = "Префикс административного деления страны по ISO 3166-2.",
                example = "UZ"
        )
        String iso3166Part2Code,

        @Schema(
                description = "Код классификатора гражданства страны.",
                example = "860"
        )
        String citizenshipCode,

        @Schema(description = "Признак мягкого удаления.", example = "false")
        Boolean deleted
) {
}
