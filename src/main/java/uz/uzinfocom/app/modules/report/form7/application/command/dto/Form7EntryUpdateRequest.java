package uz.uzinfocom.app.modules.report.form7.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "Данные для обновления ручной записи Shakl №7. Блок registered* и "
        + "primaryDiagnosisConfirmed в запросе не передаются — сервер пересчитывает их самостоятельно "
        + "по from/to и создавшей организации.")
public record Form7EntryUpdateRequest(
        @Schema(description = "Начало периода отчёта.", example = "2026-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{report.form7_entry.from.required}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @Schema(description = "Конец периода отчёта.", example = "2026-01-31", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{report.form7_entry.to.required}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,

        @Schema(description = "Davr boshida kasallanishlar soni.", example = "0")
        @PositiveOrZero(message = "{report.form7_entry.count.positive_or_zero}")
        Integer casesAtPeriodStart,

        @Schema(description = "Hisobot davrida ro'yxatga olingan — shahar aholisi.", example = "0")
        @PositiveOrZero(message = "{report.form7_entry.count.positive_or_zero}")
        Integer registeredUrbanCount,

        @Schema(description = "Hisobot davrida ro'yxatga olingan — qishloq aholisi.", example = "0")
        @PositiveOrZero(message = "{report.form7_entry.count.positive_or_zero}")
        Integer registeredRuralCount,

        @Schema(description = "Ulardan — tekshiruvdan o'tdi.", example = "0")
        @PositiveOrZero(message = "{report.form7_entry.count.positive_or_zero}")
        Integer examinedCount,

        @Schema(description = "Ulardan — tekshirilishi kerak.", example = "0")
        @PositiveOrZero(message = "{report.form7_entry.count.positive_or_zero}")
        Integer toBeExaminedCount,

        @Schema(description = "Ulardan — shifoxonaga yotqizilgan.", example = "0")
        @PositiveOrZero(message = "{report.form7_entry.count.positive_or_zero}")
        Integer hospitalizedCount,

        @Schema(description = "Davr ohirida kasallanishlar soni.", example = "0")
        @PositiveOrZero(message = "{report.form7_entry.count.positive_or_zero}")
        Integer casesAtPeriodEnd
) {
}
