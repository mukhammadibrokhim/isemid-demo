package uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Параметры поиска для справочного выбора махаллей.")
public record NeighborhoodLookupFilterRequest(
        @Schema(description = "Максимум записей в ответе. По умолчанию — 20, максимум — 200.", example = "20")
        @Min(value = 1, message = "{reference.neighborhood.filter.limit.min}")
        @Max(value = 200, message = "{reference.neighborhood.filter.limit.max}")
        Integer limit,

        @Schema(
                description = "Текст поиска по наименованию махалли на всех локалях " +
                        "(uz, uz-cyril, ru, kaa) — совпадение по любой из них возвращает элемент."
        )
        @Size(max = 255, message = "{reference.name.max_length}")
        String name
) {
}
