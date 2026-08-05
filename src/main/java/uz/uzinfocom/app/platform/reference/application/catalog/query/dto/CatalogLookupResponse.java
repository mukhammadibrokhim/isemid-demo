package uz.uzinfocom.app.platform.reference.application.catalog.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Элемент каталога для справочного выбора (select).")
public record CatalogLookupResponse(
        @Schema(description = "Внутренний идентификатор элемента каталога.", example = "1")
        Long id,
        @Schema(description = "Тип каталога.", example = "GENDER")
        String type,
        @Schema(description = "Уникальный код элемента внутри выбранного типа каталога.")
        String code,
        @Schema(description = "Необязательный код родительского элемента внутри того же типа каталога.")
        String parentCode,
        @Schema(description = "Наименование элемента каталога, локализованное по текущей локали запроса.")
        String name
) {
}
