package uz.uzinfocom.app.modules.reference.application.catalog.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Параметры поиска для справочного выбора элементов каталога.")
public record CatalogLookupFilterRequest(
        @Schema(description = "Максимум записей в ответе. По умолчанию — 20, максимум — 200.", example = "20")
        @Min(value = 1, message = "{reference.catalog.filter.limit.min}")
        @Max(value = 200, message = "{reference.catalog.filter.limit.max}")
        Integer limit,

        @Schema(
                description = "Текст поиска по наименованию элемента на всех локалях " +
                        "(uz, uz-cyril, ru, kaa) — совпадение по любой из них возвращает элемент."
        )
        @Size(max = 255, message = "{reference.name.max_length}")
        String name
) {
}
