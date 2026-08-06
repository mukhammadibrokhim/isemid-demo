package uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры пагинации и поиска для справочного выбора махаллей.")
public record NeighborhoodLookupFilterRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(value = 1, message = "{reference.neighborhood.filter.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице. По умолчанию — 20, максимум — 200.", example = "20")
        @Min(value = 1, message = "{reference.neighborhood.filter.size.min}")
        @Max(value = 200, message = "{reference.neighborhood.filter.size.max}")
        Integer size,

        @Schema(
                description = "Поле сортировки. Неподдерживаемые значения приводят к сортировке по умолчанию.",
                example = "nameUz",
                allowableValues = {"id", "code", "parentCode", "soatoId", "parentSoatoId", "nameUz", "nameUzCyril", "nameRu", "nameKaa"}
        )
        String sortBy,

        @Schema(description = "Направление сортировки.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(
                description = "Текст поиска по наименованию махалли на всех локалях " +
                        "(uz, uz-cyril, ru, kaa) — совпадение по любой из них возвращает элемент."
        )
        @Size(max = 255, message = "{reference.name.max_length}")
        String name
) implements PageableRequest {
}
