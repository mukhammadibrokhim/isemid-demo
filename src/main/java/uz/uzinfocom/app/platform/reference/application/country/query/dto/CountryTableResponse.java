package uz.uzinfocom.app.platform.reference.application.country.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Строка страны для постраничного табличного ответа.")
public record CountryTableResponse(
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
        @Schema(description = "Признак мягкого удаления.", example = "false")
        Boolean deleted
) {
}
