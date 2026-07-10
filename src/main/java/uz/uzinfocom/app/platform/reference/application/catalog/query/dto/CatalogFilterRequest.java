package uz.uzinfocom.app.platform.reference.application.catalog.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры фильтрации и пагинации таблицы каталога.")
public record CatalogFilterRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(value = 1, message = "{reference.catalog.filter.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице. Максимальное значение — 200.", example = "20")
        @Min(value = 1, message = "{reference.catalog.filter.size.min}")
        @Max(value = 200, message = "{reference.catalog.filter.size.max}")
        Integer size,

        @Schema(
                description = "Поле сортировки. Неподдерживаемые значения приводят к сортировке по умолчанию.",
                example = "nameUz",
                allowableValues = {
                        "id",
                        "type",
                        "code",
                        "parentCode",
                        "nameUz",
                        "nameRu"
                }
        )
        String sortBy,

        @Schema(description = "Направление сортировки.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(
                description = "Тип каталога.",
                example = "GENDER"
        )
        String type,

        @Schema(description = "Фильтр по точному коду элемента каталога.")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Фильтр по точному коду родительского элемента внутри того же типа каталога.")
        @Size(max = 50, message = "{reference.parent_code.max_length}")
        String parentCode,

        @Schema(description = "Текст поиска по коду элемента и локализованным наименованиям.")
        @Size(max = 255, message = "{reference.name.max_length}")
        String search
) implements PageableRequest {
}
