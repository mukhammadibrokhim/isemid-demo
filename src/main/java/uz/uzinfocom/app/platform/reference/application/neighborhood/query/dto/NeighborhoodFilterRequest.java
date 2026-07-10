package uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры фильтрации и пагинации таблицы махаллей.")
public record NeighborhoodFilterRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(value = 1, message = "{reference.neighborhood.filter.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице. Максимальное значение — 200.", example = "20")
        @Min(value = 1, message = "{reference.neighborhood.filter.size.min}")
        @Max(value = 200, message = "{reference.neighborhood.filter.size.max}")
        Integer size,

        @Schema(
                description = "Поле сортировки. Неподдерживаемые значения приводят к сортировке по умолчанию.",
                example = "nameUz",
                allowableValues = {
                        "id",
                        "code",
                        "parentCode",
                        "soatoId",
                        "parentSoatoId",
                        "nameUz",
                        "nameUzCyril",
                        "nameRu",
                        "nameKaa",
                        "createdAt",
                        "updatedAt"
                }
        )
        String sortBy,

        @Schema(description = "Направление сортировки.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(
                description = "Текст поиска по наименованиям махаллей на поддерживаемых языках.",
                example = "Dalvarzin"
        )
        @Size(max = 255, message = "{reference.name.max_length}")
        String name,

        @Schema(description = "Фильтр по точному коду махалли.", example = "AN-202001")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Фильтр по точному идентификатору СОАТО махалли.", example = "1703202001")
        @Positive(message = "{validation.must_be_positive}")
        Integer soatoId
) implements PageableRequest {
}
