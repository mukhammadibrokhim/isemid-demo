package uz.uzinfocom.app.modules.reference.application.country.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Страна для справочного выбора (select).")
public record CountryLookupResponse(
        @Schema(description = "Внутренний идентификатор страны.", example = "240")
        Long id,
        @Schema(description = "Уникальный код страны.", example = "UZB")
        String code,
        @Schema(description = "Наименование страны, локализованное по текущей локали запроса.")
        String name
) {
}
