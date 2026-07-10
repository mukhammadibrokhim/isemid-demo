package uz.uzinfocom.app.platform.reference.application.mkb10.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры фильтрации и пагинации таблицы классификатора МКБ-10.")
public record Mkb10FilterRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(value = 1, message = "{reference.mkb10.filter.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице. Максимальное значение — 200.", example = "20")
        @Min(value = 1, message = "{reference.mkb10.filter.size.min}")
        @Max(value = 200, message = "{reference.mkb10.filter.size.max}")
        Integer size,

        @Schema(
                description = "Поле сортировки. Неподдерживаемые значения приводят к сортировке по умолчанию.",
                example = "code",
                allowableValues = {
                        "id",
                        "code",
                        "level",
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

        @Schema(description = "Текст поиска по наименованиям МКБ-10 на поддерживаемых языках.", example = "Tuberkulyoz")
        @Size(max = 255, message = "{reference.name.max_length}")
        String name,

        @Schema(description = "Фильтр по точному коду МКБ-10.", example = "A15")
        @Size(max = 20, message = "{reference.mkb10.code.max_length}")
        String code,

        @Schema(description = "Фильтр по внешнему идентификатору непосредственного родителя.", example = "12")
        @Positive(message = "{reference.mkb10.parent_id.positive}")
        Long parentId,

        @Schema(description = "Фильтр по глубине иерархии.", example = "3")
        Integer level
) implements PageableRequest {
}
