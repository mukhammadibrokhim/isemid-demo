package uz.uzinfocom.app.platform.reference.application.region.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры пагинации и поиска для справочного выбора регионов.")
public record RegionLookupFilterRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(value = 1, message = "{reference.region.filter.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице. По умолчанию — 20, максимум — 200.", example = "20")
        @Min(value = 1, message = "{reference.region.filter.size.min}")
        @Max(value = 200, message = "{reference.region.filter.size.max}")
        Integer size,

        @Schema(
                description = "Поле сортировки. Неподдерживаемые значения приводят к сортировке по умолчанию.",
                example = "nameUz",
                allowableValues = {"id", "code", "parentCode", "soatoId", "nameUz", "nameUzCyril", "nameRu", "nameKaa"}
        )
        String sortBy,

        @Schema(description = "Направление сортировки.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(
                description = "Текст поиска по наименованию региона на всех локалях " +
                        "(uz, uz-cyril, ru, kaa) — совпадение по любой из них возвращает элемент."
        )
        @Size(max = 255, message = "{reference.name.max_length}")
        String name
) implements PageableRequest {
}
