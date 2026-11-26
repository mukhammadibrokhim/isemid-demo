package uz.uzinfocom.app.modules.reference.application.manualreport.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры фильтрации и пагинации таблицы ручных отчётов.")
public record ManualReportFilterRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(value = 1, message = "{reference.manual_report.filter.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице. Максимальное значение — 200.", example = "20")
        @Min(value = 1, message = "{reference.manual_report.filter.size.min}")
        @Max(value = 200, message = "{reference.manual_report.filter.size.max}")
        Integer size,

        @Schema(
                description = "Поле сортировки. Неподдерживаемые значения приводят к сортировке по умолчанию.",
                example = "nameUz",
                allowableValues = {
                        "id",
                        "code",
                        "shortName",
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
                description = "Текст поиска по наименованиям ручных отчётов на поддерживаемых языках.",
                example = "Sil"
        )
        @Size(max = 255, message = "{reference.name.max_length}")
        String name,

        @Schema(description = "Фильтр по точному коду ручного отчёта.", example = "TUBERCULOSIS")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Фильтр по тегу типа отчёта (без учёта регистра).", example = "FORM_12")
        @Size(max = 50, message = "{reference.code.max_length}")
        String reportType,

        @Schema(description = "Фильтр по точному коду МКБ-10, входящему в отчёт (без учёта регистра).", example = "A15.0")
        @Size(max = 20, message = "{reference.code.max_length}")
        String icd10Code
) implements PageableRequest {
}
