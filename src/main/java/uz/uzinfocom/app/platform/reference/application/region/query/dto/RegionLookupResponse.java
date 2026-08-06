package uz.uzinfocom.app.platform.reference.application.region.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Регион для справочного выбора (select).")
public record RegionLookupResponse(
        @Schema(description = "Внутренний идентификатор региона.", example = "1")
        Long id,
        @Schema(description = "Уникальный код региона.", example = "UZ-AN")
        String code,
        @Schema(description = "Код родительской страны для данного региона.", example = "UZ")
        String parentCode,
        @Schema(description = "Идентификатор СОАТО региона.", example = "1703")
        Integer soatoId,
        @Schema(description = "Наименование региона, локализованное по текущей локали запроса.")
        String name
) {
}
