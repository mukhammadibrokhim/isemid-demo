package uz.uzinfocom.app.modules.report.form7.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

import java.time.LocalDate;

@Schema(description = "Параметры фильтрации и пагинации таблицы ручных записей Shakl №7.")
public record Form7EntryFilterRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(value = 1, message = "{report.form7_entry.filter.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице. Максимальное значение — 200.", example = "20")
        @Min(value = 1, message = "{report.form7_entry.filter.size.min}")
        @Max(value = 200, message = "{report.form7_entry.filter.size.max}")
        Integer size,

        @Schema(
                description = "Поле сортировки. Неподдерживаемые значения приводят к сортировке по умолчанию.",
                example = "createdAt",
                allowableValues = {"id", "fromDate", "toDate", "createdAt", "updatedAt"}
        )
        String sortBy,

        @Schema(description = "Направление сортировки.", example = "desc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Начало периода — записи, чей период пересекается с этой датой и позже, включительно.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @Schema(description = "Конец периода — записи, чей период пересекается с этой датой и раньше, включительно.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) implements PageableRequest {
}
